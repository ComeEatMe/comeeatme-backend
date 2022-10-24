package com.comeeatme.security.oauth2;

import com.comeeatme.domain.account.Account;
import com.comeeatme.domain.account.repository.AccountRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.member.service.MemberNicknameCreator;
import com.comeeatme.security.dto.LoginResponse;
import com.comeeatme.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandlerCustom implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    private final JwtTokenProvider jwtTokenProvider;

    private final AccountRepository accountRepository;

    private final MemberRepository memberRepository;

    private final MemberNicknameCreator nicknameCreator;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        OAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        String username = oAuth2User.getName();

        if (isNotAccountExists(username)) {
            createMemberOf(username);
        }

        String accessToken = jwtTokenProvider.createAccessToken(username);
        String refreshToken = jwtTokenProvider.createRefreshToken(username);

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("username = " + username));
        account.setRefreshToken(refreshToken);
        accountRepository.save(account);

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .memberId(account.getMember().getId())
                .build();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getWriter(), loginResponse);
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
