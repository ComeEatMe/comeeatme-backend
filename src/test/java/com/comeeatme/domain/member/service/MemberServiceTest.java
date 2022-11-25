package com.comeeatme.domain.member.service;

import com.comeeatme.domain.common.response.DeleteResult;
import com.comeeatme.domain.common.response.DuplicateResult;
import com.comeeatme.domain.common.response.UpdateResult;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.image.repository.ImageRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.member.request.MemberEdit;
import com.comeeatme.domain.member.request.MemberSearch;
import com.comeeatme.domain.member.response.MemberDetailDto;
import com.comeeatme.domain.member.response.MemberSimpleDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

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
        Member member = Member.builder()
                .id(1L)
                .nickname("nickname")
                .introduction("introduction")
                .build();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        MemberEdit memberEdit = MemberEdit.builder()
                .nickname("edited-nickname")
                .introduction("edited-introduction")
                .build();

        // when
        UpdateResult<Long> updateResult = memberService.edit(memberEdit, 1L);

        // then
        then(imageRepository).should(never()).findById(anyLong());
        assertThat(updateResult.getId()).isEqualTo(member.getId());
        assertThat(member.getNickname()).isEqualTo("edited-nickname");
        assertThat(member.getIntroduction()).isEqualTo("edited-introduction");
    }

    @Test
    void editImage() {
        // given
        Image image = mock(Image.class);
        Member member = Member.builder()
                .id(1L)
                .nickname("nickname")
                .introduction("introduction")
                .image(image)
                .build();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        Image editedImage = mock(Image.class);
        given(editedImage.getUseYn()).willReturn(true);
        given(imageRepository.findById(2L)).willReturn(Optional.of(editedImage));

        // when
        UpdateResult<Long> result = memberService.editImage(1L, 2L);

        // then
        then(image).should().delete();
        assertThat(member.getImage()).isEqualTo(editedImage);
        assertThat(result.getId()).isEqualTo(member.getId());
    }

    @Test
    void deleteImage() {
        // given
        Image image = mock(Image.class);
        Member member = Member.builder()
                .id(1L)
                .nickname("nickname")
                .introduction("introduction")
                .image(image)
                .build();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        // when
        DeleteResult<Long> result = memberService.deleteImage(1L);

        // then
        then(image).should().delete();
        assertThat(member.getImage()).isNull();
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
}