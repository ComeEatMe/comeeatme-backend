package com.comeeatme.security.oauth2;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;

import java.util.Map;

public interface OAuth2UserInfo {

    Oauth2Provider getProvider();

    String getProviderId();

    static OAuth2UserInfo of(OAuth2UserRequest userRequest, Map<String, Object> attributes) {
        String clientId = userRequest.getClientRegistration().getRegistrationId();
        if ("kakao".equals(clientId)) {
            return KakaoUserInfo.builder()
                    .providerId(attributes.get("id").toString())
                    .build();
        } else {
            throw new IllegalArgumentException("지원하지 않는 OAuth2 Provider 입니다. provider = " + clientId);
        }
    }
}
