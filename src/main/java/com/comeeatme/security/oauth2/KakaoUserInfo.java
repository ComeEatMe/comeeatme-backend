package com.comeeatme.security.oauth2;

import lombok.Builder;

public class KakaoUserInfo implements OAuth2UserInfo {

    private static final Oauth2Provider provider = Oauth2Provider.KAKAO;

    private final String providerId;

    @Builder
    public KakaoUserInfo(String providerId) {
        this.providerId = providerId;
    }

    @Override
    public Oauth2Provider getProvider() {
        return provider;
    }

    @Override
    public String getProviderId() {
        return providerId;
    }
}
