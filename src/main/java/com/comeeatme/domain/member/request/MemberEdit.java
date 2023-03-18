package com.comeeatme.domain.member.request;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberEdit {

    @NotBlank
    @Size(max = 15)
    private String nickname;

    @NotNull
    @Size(max = 100)
    private String introduction;
}
