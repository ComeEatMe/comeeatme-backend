package com.comeeatme.domain.member.response;

import com.comeeatme.domain.images.Images;
import com.comeeatme.domain.member.Member;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class MemberSimpleDtoTest {

    @Test
    void of() {
        // given
        Images image = mock(Images.class);
        given(image.getUseYn()).willReturn(true);
        given(image.getUrl()).willReturn("image-url");
        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        given(member.getNickname()).willReturn("nickname");
        given(member.getImage()).willReturn(image);

        // when
        MemberSimpleDto result = MemberSimpleDto.of(member);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNickname()).isEqualTo("nickname");
        assertThat(result.getImageUrl()).isEqualTo("image-url");
    }

    @Test
    void of_ImageDeleted() {
        // given
        Images image = mock(Images.class);
        given(image.getUseYn()).willReturn(false);
        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        given(member.getNickname()).willReturn("nickname");
        given(member.getImage()).willReturn(image);

        // when
        MemberSimpleDto result = MemberSimpleDto.of(member);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNickname()).isEqualTo("nickname");
        assertThat(result.getImageUrl()).isNull();
    }

    @Test
    void of_ImageNull() {
        // given
        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        given(member.getNickname()).willReturn("nickname");
        given(member.getImage()).willReturn(null);

        // when
        MemberSimpleDto result = MemberSimpleDto.of(member);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNickname()).isEqualTo("nickname");
        assertThat(result.getImageUrl()).isNull();
    }

}