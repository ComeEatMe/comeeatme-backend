package com.comeeatme.domain.member;

import com.comeeatme.domain.images.Images;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberEditor {

    private String nickname;

    private String introduction;

    private Images image;

    @Builder
    private MemberEditor(String nickname, String introduction, Images image) {
        this.nickname = nickname;
        this.introduction = introduction;
        this.image = image;
    }
}
