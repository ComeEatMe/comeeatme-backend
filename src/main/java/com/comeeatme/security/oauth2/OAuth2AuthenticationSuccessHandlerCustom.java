package com.comeeatme.security.oauth2;

import com.comeeatme.domain.account.Account;
import com.comeeatme.domain.account.repository.AccountRepository;
import com.comeeatme.security.LoginResponse;
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

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        OAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        String username = oAuth2User.getName();

        String accessToken = jwtTokenProvider.createAccessToken(username);
        String refreshToken = jwtTokenProvider.createRefreshToken(username);
        LoginResponse loginResponse = new LoginResponse(accessToken, refreshToken);

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("username = " + username));
        account.setRefreshToken(refreshToken);
        accountRepository.saveAndFlush(account);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getWriter(), loginResponse);
    }
}