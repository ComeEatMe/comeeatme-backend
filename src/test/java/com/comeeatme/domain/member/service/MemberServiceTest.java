package com.comeeatme.domain.member.service;

import com.comeeatme.domain.common.response.CreateResult;
import com.comeeatme.domain.common.response.DeleteResult;
import com.comeeatme.domain.common.response.DuplicateResult;
import com.comeeatme.domain.common.response.UpdateResult;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.image.repository.ImageRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.MemberDeleteReason;
import com.comeeatme.domain.member.MemberEditor;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.member.request.MemberEdit;
import com.comeeatme.domain.member.request.MemberSearch;
import com.comeeatme.domain.member.response.MemberDetailDto;
import com.comeeatme.domain.member.response.MemberSimpleDto;
import com.comeeatme.error.exception.AlreadyNicknameExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ImageRepository imageRepository;

    @Test
    void edit() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        MemberEditor.MemberEditorBuilder editorBuilder = mock(MemberEditor.MemberEditorBuilder.class);
        given(member.toEditor()).willReturn(editorBuilder);
        given(editorBuilder.nickname("edited-nickname")).willReturn(editorBuilder);
        given(editorBuilder.introduction("edited-introduction")).willReturn(editorBuilder);
        MemberEditor editor = mock(MemberEditor.class);
        given(editorBuilder.build()).willReturn(editor);

        MemberEdit memberEdit = MemberEdit.builder()
                .nickname("edited-nickname")
                .introduction("edited-introduction")
                .build();

        // when
        UpdateResult<Long> updateResult = memberService.edit(memberEdit, 1L);

        // then
        assertThat(updateResult.getId()).isEqualTo(member.getId());
        then(member).should().edit(editor);
    }

    @Test
    void editImage() {
        // given
        Image image = mock(Image.class);
        Member member = mock(Member.class);
        given(member.getImage()).willReturn(image);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        Image editedImage = mock(Image.class);
        given(editedImage.getUseYn()).willReturn(true);
        given(imageRepository.findById(2L)).willReturn(Optional.of(editedImage));

        MemberEditor.MemberEditorBuilder editorBuilder = mock(MemberEditor.MemberEditorBuilder.class);
        given(member.toEditor()).willReturn(editorBuilder);
        given(editorBuilder.image(editedImage)).willReturn(editorBuilder);
        MemberEditor editor = mock(MemberEditor.class);
        given(editorBuilder.build()).willReturn(editor);

        // when
        UpdateResult<Long> result = memberService.editImage(1L, 2L);

        // then
        then(image).should().delete();
        then(member).should().edit(editor);
        assertThat(result.getId()).isEqualTo(member.getId());
    }

    @Test
    void deleteImage() {
        // given
        Image image = mock(Image.class);
        Member member = mock(Member.class);
        given(member.getImage()).willReturn(image);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        MemberEditor.MemberEditorBuilder editorBuilder = mock(MemberEditor.MemberEditorBuilder.class);
        given(member.toEditor()).willReturn(editorBuilder);
        given(editorBuilder.image(null)).willReturn(editorBuilder);
        MemberEditor editor = mock(MemberEditor.class);
        given(editorBuilder.build()).willReturn(editor);

        // when
        DeleteResult<Long> result = memberService.deleteImage(1L);

        // then
        then(image).should().delete();
        assertThat(result.getId()).isEqualTo(member.getId());
    }

    @Test
    void checkNicknameDuplicate() {
        // given
        given(memberRepository.existsByNickname("username")).willReturn(true);

        // when
        DuplicateResult result = memberService.checkNicknameDuplicate("username");

        // then
        assertThat(result.isDuplicate()).isTrue();
    }

    @Test
    void search() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);
        given(memberRepository.findSliceWithImagesByNicknameStartingWith(pageRequest, "nickname"))
                .willReturn(new SliceImpl<>(List.of(mock(Member.class), mock(Member.class))));

        // when
        MemberSearch memberSearch = MemberSearch.builder()
                .nickname("nickname")
                .build();
        Slice<MemberSimpleDto> result = memberService.search(pageRequest, memberSearch);

        // then
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void get() {
        // given
        Image image = mock(Image.class);
        given(image.getUseYn()).willReturn(true);
        given(image.getUrl()).willReturn("image-url");
        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        given(member.getUseYn()).willReturn(true);
        given(member.getNickname()).willReturn("nickname");
        given(member.getIntroduction()).willReturn("introduction");
        given(member.getImage()).willReturn(image);

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));


        // when
        MemberDetailDto dto = memberService.get(1L);

        // then
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getNickname()).isEqualTo("nickname");
        assertThat(dto.getIntroduction()).isEqualTo("introduction");
        assertThat(dto.getImageUrl()).isEqualTo("image-url");
    }

    @Test
    void create() {
        // given
        given(memberRepository.existsByNickname("nickname")).willReturn(false);
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);

        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        given(memberRepository.save(memberCaptor.capture())).willReturn(member);

        // when
        CreateResult<Long> result = memberService.create("nickname");

        // then
        assertThat(result.getId()).isEqualTo(1L);

        Member captorValue = memberCaptor.getValue();
        assertThat(captorValue.getImage()).isNull();
        assertThat(captorValue.getId()).isNull();
        assertThat(captorValue.getUseYn()).isTrue();
        assertThat(captorValue.getIntroduction()).isEmpty();
        assertThat(captorValue.getNickname()).isEqualTo("nickname");
    }

    @Test
    void create_NicknameExists() {
        // given
        given(memberRepository.existsByNickname("nickname")).willReturn(true);

        // when
        assertThatThrownBy(() -> memberService.create("nickname"))
                .isInstanceOf(AlreadyNicknameExistsException.class);
    }

    @Test
    void delete() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(member.getId()).willReturn(1L);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        // when
        DeleteResult<Long> result = memberService.delete(1L);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        then(member).should().delete();
    }

    @Test
    void registerDeleteReason() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        // when
        memberService.registerDeleteReason(1L, MemberDeleteReason.NO_INFORMATION);

        // then
        then(member).should().setDeleteReason(MemberDeleteReason.NO_INFORMATION);
    }
}