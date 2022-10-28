package com.comeeatme.domain.member.response;

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
public class MemberDetailDto {

    private Long id;

    private String nickname;

    private String introduction;

    private String imageUrl;

    public static MemberDetailDto of(Member member) {
        return MemberDetailDto.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .introduction(member.getIntroduction())
                .imageUrl(Optional.ofNullable(member.getImage())
                        .filter(Image::getUseYn)
                        .map(Image::getUrl)
                        .orElse(null))
                .build();
    }

    @Builder
    private MemberDetailDto(Long id, String nickname, String introduction, @Nullable String imageUrl) {
        this.id = id;
        this.nickname = nickname;
        this.introduction = introduction;
        this.imageUrl = imageUrl;
    }
}
