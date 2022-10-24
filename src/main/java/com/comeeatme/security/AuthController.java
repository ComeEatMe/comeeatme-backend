package com.comeeatme.security;

import com.comeeatme.domain.account.Account;
import com.comeeatme.domain.account.repository.AccountRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.member.service.MemberNicknameCreator;
import com.comeeatme.security.request.OauthLogin;
import com.comeeatme.security.response.LoginResponse;
import com.comeeatme.security.jwt.JwtTokenProvider;
import com.comeeatme.security.oauth2.OAuth2UserInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    private final AccountRepository accountRepository;

    private final MemberRepository memberRepository;

    private final MemberNicknameCreator nicknameCreator;

    private final ObjectMapper objectMapper;

    @PostMapping("/login/oauth2/token/{registrationId}")
    public ResponseEntity<LoginResponse> loginOauthToken(
            @PathVariable String registrationId, @Valid @RequestBody OauthLogin oauthLogin)
            throws JsonProcessingException {
        String providerId = getProviderId(registrationId, oauthLogin.getAccessToken());
        OAuth2UserInfo userInfo = OAuth2UserInfo.of(registrationId, providerId);
        String username = userInfo.ofUsername();

        if (isNotAccountExists(username)) {
            createMemberOf(username);
        }

        String jwtAccessToken = jwtTokenProvider.createAccessToken(username);
        String jwtRefreshToken = jwtTokenProvider.createRefreshToken(username);

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("username = " + username));
        account.setRefreshToken(jwtRefreshToken);
        accountRepository.save(account);

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(jwtAccessToken)
                .refreshToken(jwtRefreshToken)
                .memberId(account.getMember().getId())
                .build();
        return ResponseEntity.ok(loginResponse);
    }

    private String getProviderId(String registrationId, String accessToken) throws JsonProcessingException {
        WebClient webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .build();
        String tokenInfoJson = webClient.get()
                .uri(getTokenInfoUrl(registrationId))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return objectMapper.readValue(tokenInfoJson, Map.class).get("id").toString();
    }

    private String getTokenInfoUrl(String registrationId) {
        if ("kakao".equals(registrationId)) {
            return "https://kapi.kakao.com/v1/user/access_token_info";
        }
        throw new IllegalArgumentException("지원하지 않는 OAuth2 Provider 입니다. provider = " + registrationId);
    }

    private boolean isNotAccountExists(String username) {
        return accountRepository.findByUsername(username).filter(Account::getUseYn).isEmpty();
    }

    private void createMemberOf(String username) {
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

}
