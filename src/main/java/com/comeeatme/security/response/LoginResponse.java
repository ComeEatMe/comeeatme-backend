package com.comeeatme.security.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginResponse {

    private Long memberId;

    private String accessToken;

    private String refreshToken;

    @Builder
    private LoginResponse(Long memberId, String accessToken, String refreshToken) {
        this.memberId = memberId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
