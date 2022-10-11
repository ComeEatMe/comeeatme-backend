package com.comeeatme.domain.member.request;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberSearch {

    @NotBlank
    @Size(min = 1, max = 15)
    private String nickname;
}
