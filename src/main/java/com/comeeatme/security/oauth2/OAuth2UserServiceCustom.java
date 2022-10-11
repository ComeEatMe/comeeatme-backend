package com.comeeatme.security.oauth2;

import com.comeeatme.domain.account.Account;
import com.comeeatme.domain.account.repository.AccountRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.member.service.MemberNicknameCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2UserServiceCustom extends DefaultOAuth2UserService {

    private final AccountRepository accountRepository;

    private final MemberRepository memberRepository;

    private final MemberNicknameCreator nicknameCreator;

    private static final String NAME_ATTRIBUTE_KEY = "username";

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);
        OAuth2UserInfo userInfo = OAuth2UserInfo.of(userRequest, user.getAttributes());
        String username = usernameFrom(userInfo.getProvider(), userInfo.getProviderId());
        if (isNotAccountExists(username)) {
            String nickname = nicknameCreator.create();
            while (memberRepository.existsByNickname(nickname)) {
                nickname = nicknameCreator.create();
            }
            Member member = memberRepository.save(Member.builder()
                    .nickname(nickname)
                    .introduction("")
                    .build());
            accountRepository.save(Account.builder()
                    .member(member)
                    .username(username)
                    .build());
        }
        Map<String, Object> attributes = Map.of(
                NAME_ATTRIBUTE_KEY, username
        );
        return new DefaultOAuth2User(null, attributes, NAME_ATTRIBUTE_KEY);
    }

    private boolean isNotAccountExists(String username) {
        return accountRepository.findByUsername(username).filter(Account::getUseYn).isEmpty();
    }

    private String usernameFrom(Oauth2Provider provider, String providerId) {
        return provider + "-" + providerId;
    }
}
