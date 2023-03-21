package com.comeeatme.api.member.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Builder
@AllArgsConstructor
public class MemberSearch {

    @NotBlank
    @Size(min = 1, max = 15)
    private String nickname;
}
