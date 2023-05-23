package com.comeeatme.domain.member;

import com.comeeatme.domain.image.Image;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberEditor {

    private String nickname;

    private String introduction;

    private Image image;

    @Builder
    private MemberEditor(String nickname, String introduction, Image image) {
        this.nickname = nickname;
        this.introduction = introduction;
        this.image = image;
    }
}
