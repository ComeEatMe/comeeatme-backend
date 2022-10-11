package com.comeeatme.domain.member.service;

import com.comeeatme.domain.common.response.DuplicateResult;
import com.comeeatme.domain.images.Images;
import com.comeeatme.domain.images.repository.ImagesRepository;
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
    private ImagesRepository imagesRepository;

    @Test
    void edit_EqualMemberImageAndImageNull() {
        // given
        Member member = Member.builder()
                .id(1L)
                .nickname("nickname")
                .introduction("introduction")
                .build();
        given(memberRepository.findByUsername("username")).willReturn(Optional.of(member));

        MemberEdit memberEdit = MemberEdit.builder()
                .nickname("edited-nickname")
                .introduction("edited-introduction")
                .imageId(null)
                .build();

        // when
        Long editedId = memberService.edit(memberEdit, "username");

        // then
        then(imagesRepository).should(never()).findById(anyLong());
        assertThat(editedId).isEqualTo(member.getId());
        assertThat(member.getNickname()).isEqualTo("edited-nickname");
        assertThat(member.getIntroduction()).isEqualTo("edited-introduction");
    }

    @Test
    void edit_EqualMemberImageAndImage() {
        // given
        Images image = mock(Images.class);
        given(image.getUseYn()).willReturn(true);
        given(image.getId()).willReturn(2L);

        Member member = Member.builder()
                .id(1L)
                .nickname("nickname")
                .introduction("introduction")
                .image(image)
                .build();
        given(memberRepository.findByUsername("username")).willReturn(Optional.of(member));

        MemberEdit memberEdit = MemberEdit.builder()
                .nickname("edited-nickname")
                .introduction("edited-introduction")
                .imageId(2L)
                .build();

        // when
        Long editedId = memberService.edit(memberEdit, "username");

        // then
        then(image).should(never()).delete();
        then(imagesRepository).should(never()).findById(anyLong());
        assertThat(editedId).isEqualTo(member.getId());
        assertThat(member.getNickname()).isEqualTo("edited-nickname");
        assertThat(member.getIntroduction()).isEqualTo("edited-introduction");
    }

    @Test
    void edit_DiffMemberImageAndImage() {
        // given
        Images memberImage = mock(Images.class);
        given(memberImage.getUseYn()).willReturn(true);
        given(memberImage.getId()).willReturn(2L);

        Member member = Member.builder()
                .id(1L)
                .nickname("nickname")
                .introduction("introduction")
                .image(memberImage)
                .build();
        given(memberRepository.findByUsername("username")).willReturn(Optional.of(member));

        Member imageMember = mock(Member.class);
        given(imageMember.getId()).willReturn(1L);
        Images editedImage = mock(Images.class);
        given(editedImage.getUseYn()).willReturn(true);
        given(editedImage.getMember()).willReturn(imageMember);
        given(imagesRepository.findById(3L)).willReturn(Optional.of(editedImage));

        MemberEdit memberEdit = MemberEdit.builder()
                .nickname("edited-nickname")
                .introduction("edited-introduction")
                .imageId(3L)
                .build();

        // when
        Long editedId = memberService.edit(memberEdit, "username");

        // then
        then(memberImage).should().delete();
        assertThat(editedId).isEqualTo(member.getId());
        assertThat(member.getNickname()).isEqualTo("edited-nickname");
        assertThat(member.getIntroduction()).isEqualTo("edited-introduction");
        assertThat(member.getImage()).isEqualTo(editedImage);
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
        Images image = mock(Images.class);
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