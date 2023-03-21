package com.comeeatme.web.security.request;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OauthLogin {

    @NotBlank
    private String accessToken;
}
