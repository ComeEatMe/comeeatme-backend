package com.comeeatme.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Author      : 문 윤지
 * Description : Jwt 생성 및 인증 정보 추출
 * History     : [2022-07-15] 문윤지 - Class Create
 */
@Slf4j
@Component
public class JwtTokenProvider {

    public static final String TOKEN_PREFIX = "Bearer ";

    private static final String TOKEN_TYPE = "token_type";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";

    private final SecretKey secretKey;

    private final long accessTokenValidityInMillis;

    private final long refreshTokenValidityInMillis;

    private final UserDetailsService userDetailsService;

    private final JwtParser jwtParser;

    public JwtTokenProvider(
            @Value("${jwt.access-token-validity}") long accessTokenValidity,
            @Value("${jwt.refresh-token-validity}") long refreshTokenValidity,
            @Value("${jwt.secret}") String secret,
            UserDetailsService userDetailsService) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenValidityInMillis = accessTokenValidity * 1000;
        this.refreshTokenValidityInMillis = refreshTokenValidity * 1000;
        this.userDetailsService = userDetailsService;
        this.jwtParser = Jwts.parserBuilder().setSigningKey(secretKey).build();
    }

    public String createAccessToken(String username) {
        return createToken(username, ACCESS_TOKEN);
    }

    public String createRefreshToken(String username) {
        return createToken(username, REFRESH_TOKEN);
    }

    private String createToken(String username, String tokenType) {
        long expiredTimeMillis = tokenType.equals(ACCESS_TOKEN) ?
                accessTokenValidityInMillis : refreshTokenValidityInMillis;
        return Jwts.builder()
                .setSubject(username)
                .claim(TOKEN_TYPE, tokenType)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiredTimeMillis))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 유효성 및 만료기간 검사
    public boolean validateToken(String token) {
        try {
            jwtParser.parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT signature.");
            log.trace("Invalid JWT signature trace: ", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token.");
            log.trace("Expired JWT token trace: ", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token.");
            log.trace("Unsupported JWT token trace: ", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT token compact of handler are invalid.");
            log.trace("JWT token compact of handler are invalid trace: ", e);
        }
        return false;
    }

    // 토큰에서 인증 정보 추출
    public Authentication getAuthentication(String accessToken) {
        String usernameFromToken = jwtParser.parseClaimsJws(accessToken).getBody().getSubject();
        UserDetails userDetails = userDetailsService.loadUserByUsername(usernameFromToken);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    public String getSubject(String token) {
        return jwtParser.parseClaimsJws(token).getBody().getSubject();
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtTokenProvider.TOKEN_PREFIX)) {
            return bearerToken.substring(JwtTokenProvider.TOKEN_PREFIX.length());
        }
        return null;
    }
}
