package com.comeeatme.security.oauth2;

import com.comeeatme.domain.member.Member;
import com.comeeatme.security.account.Account;
import com.comeeatme.security.account.service.AccountService;
import com.comeeatme.security.jwt.JwtTokenProvider;
import com.comeeatme.security.response.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandlerCustom implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    private final JwtTokenProvider jwtTokenProvider;

    private final AccountService accountService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        OAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        String username = oAuth2User.getName();

        String accessToken = jwtTokenProvider.createAccessToken(username);
        LocalDateTime accessTokenExpiresAt = LocalDateTime.ofInstant(
                jwtTokenProvider.getExpiration(accessToken).toInstant(), ZoneId.systemDefault());
        String refreshToken = jwtTokenProvider.createRefreshToken(username);
        LocalDateTime refreshTokenExpiresAt = LocalDateTime.ofInstant(
                jwtTokenProvider.getExpiration(refreshToken).toInstant(), ZoneId.systemDefault());

        Account account = accountService.login(username, refreshToken, refreshTokenExpiresAt);

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(accessToken)
                .accessTokenExpiresAt(accessTokenExpiresAt)
                .refreshToken(refreshToken)
                .refreshTokenExpiresAt(refreshTokenExpiresAt)
                .memberId(Optional.ofNullable(account.getMember())
                        .map(Member::getId)
                        .orElse(null))
                .build();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getWriter(), loginResponse);
    }

}
