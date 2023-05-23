package com.comeeatme.api.member.response;

import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.member.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.Optional;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberSimpleDto {

    private Long id;

    private String nickname;

    private String imageUrl;

    public static MemberSimpleDto of(Member member) {
        return MemberSimpleDto.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .imageUrl(Optional.ofNullable(member.getImage())
                        .filter(Image::getUseYn)
                        .map(Image::getUrl)
                        .orElse(null))
                .build();
    }

    @Builder
    private MemberSimpleDto(Long id, String nickname, @Nullable String imageUrl) {
        this.id = id;
        this.nickname = nickname;
        this.imageUrl = imageUrl;
    }
}
