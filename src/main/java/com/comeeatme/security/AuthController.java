package com.comeeatme.security;

import com.comeeatme.api.common.response.ApiResult;
import com.comeeatme.domain.member.Member;
import com.comeeatme.error.dto.ErrorResponse;
import com.comeeatme.error.exception.ErrorCode;
import com.comeeatme.security.account.Account;
import com.comeeatme.security.account.service.AccountService;
import com.comeeatme.security.jwt.JwtTokenProvider;
import com.comeeatme.security.oauth2.OAuth2UserInfo;
import com.comeeatme.security.request.OauthLogin;
import com.comeeatme.security.response.LoginResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper;

    private final AccountService accountService;

    @PostMapping("/login/oauth2/token/{registrationId}")
    public ResponseEntity<LoginResponse> loginOauthToken(
            @PathVariable String registrationId, @Valid @RequestBody OauthLogin oauthLogin)
            throws JsonProcessingException {
        String providerId = getProviderId(registrationId, oauthLogin.getAccessToken());
        OAuth2UserInfo userInfo = OAuth2UserInfo.of(registrationId, providerId);
        String username = userInfo.ofUsername();

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

    @PostMapping("/login/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (!StringUtils.hasText(token)) {
            return unauthorizedResponse();
        }

        String username = jwtTokenProvider.getSubject(token);
        Account account = accountService.get(username);
        if (!token.equals(account.getRefreshToken())) {
            return unauthorizedResponse();
        }

        String accessToken = jwtTokenProvider.createAccessToken(username);
        LocalDateTime accessTokenExpiresAt = LocalDateTime.ofInstant(
                jwtTokenProvider.getExpiration(accessToken).toInstant(), ZoneId.systemDefault());
        String refreshToken = account.getRefreshToken();
        LocalDateTime refreshTokenExpiresAt = account.getRefreshTokenExpiresAt();
        long refreshTokenValidityInDay = jwtTokenProvider.getRefreshTokenValidityInDay();
        long loginDayTerm = Duration.between(
                LocalDateTime.now().plusDays(refreshTokenValidityInDay), refreshTokenExpiresAt).toDays();
        if (loginDayTerm > 0) {
            refreshToken = jwtTokenProvider.createRefreshToken(username);
            refreshTokenExpiresAt = LocalDateTime.ofInstant(
                    jwtTokenProvider.getExpiration(refreshToken).toInstant(), ZoneId.systemDefault());
            accountService.login(username, refreshToken, refreshTokenExpiresAt);
        }

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(accessToken)
                .accessTokenExpiresAt(accessTokenExpiresAt)
                .refreshToken(refreshToken)
                .refreshTokenExpiresAt(refreshTokenExpiresAt)
                .memberId(Optional.ofNullable(account.getMember())
                        .map(Member::getId)
                        .orElse(null))
                .build();
        return ResponseEntity.ok(loginResponse);
    }

    private static ResponseEntity<ApiResult<?>> unauthorizedResponse() {
        ErrorCode errorCode = ErrorCode.HANDLE_UNAUTHORIZED;
        ErrorResponse errorResponseDto = ErrorResponse.of(errorCode);
        ApiResult<Void> result = ApiResult.<Void>builder()
                .success(false)
                .error(errorResponseDto)
                .build();
        return ResponseEntity.status(errorCode.getStatus()).body(result);
    }

}
