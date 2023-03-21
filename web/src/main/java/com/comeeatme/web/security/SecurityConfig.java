package com.comeeatme.web.security;

import com.comeeatme.api.account.AccountService;
import com.comeeatme.web.security.jwt.*;
import com.comeeatme.web.security.oauth2.OAuth2AuthenticationSuccessHandlerCustom;
import com.comeeatme.web.security.oauth2.OAuth2UserServiceCustom;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] GET_PERMITTED_URLS = {
            "/code",
    };

    private static final String[] POST_PERMITTED_URLS = {
            "/login/oauth2/token/{registrationId}",
            "/login/reissue"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity,
            CorsConfigurationSource corsConfigurationSource,
            OAuth2UserServiceCustom oAuth2UserService,
            OAuth2AuthenticationSuccessHandlerCustom oAuth2AuthenticationSuccessHandler,
            JwtAuthenticationCheckFilter jwtAuthenticationCheckFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtLogoutHandler jwtLogoutHandler,
            JwtLogoutSuccessHandler jwtLogoutSuccessHandler)
            throws Exception {
        return httpSecurity
                .cors()
                .configurationSource(corsConfigurationSource)
                .and()

                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                .formLogin().disable()
                .httpBasic().disable()

                .addFilterAfter(jwtAuthenticationCheckFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(config -> config
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                .logout(logout -> logout
                        .addLogoutHandler(jwtLogoutHandler)
                        .logoutSuccessHandler(jwtLogoutSuccessHandler)
                )

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint()
                        .userService(oAuth2UserService)

                        .and()
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                )

                .authorizeRequests(antz -> antz
                        .antMatchers(HttpMethod.GET, GET_PERMITTED_URLS).permitAll()
                        .antMatchers(HttpMethod.POST, POST_PERMITTED_URLS).permitAll()
                        .anyRequest().authenticated()
                )

                .build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().antMatchers("/docs/**", "/agreement/**");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        String[] clientUrls = {
                "http://localhost:3000",
                "https://comeeatme.zooneon.dev"
        };
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Arrays.asList(clientUrls));
        corsConfig.addAllowedHeader(CorsConfiguration.ALL);
        corsConfig.addAllowedMethod(CorsConfiguration.ALL);
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return source;
    }

    @Bean
    public JwtTokenProvider jwtTokenProvider(
            @Value("${jwt.access-token-validity}") long accessTokenValidity,
            @Value("${jwt.refresh-token-validity}") long refreshTokenValidity,
            @Value("${jwt.secret}") String secret) {
        return new JwtTokenProvider(
                accessTokenValidity, refreshTokenValidity, secret);
    }

    @Bean
    public JwtAuthenticationCheckFilter jwtAuthenticationCheckFilter(
            JwtTokenProvider jwtTokenProvider) {
        return new JwtAuthenticationCheckFilter(
                jwtTokenProvider);
    }

    @Bean
    public JwtLogoutHandler jwtLogoutHandler(JwtTokenProvider jwtTokenProvider, AccountService accountService) {
        return new JwtLogoutHandler(jwtTokenProvider, accountService);
    }

    @Bean
    public JwtLogoutSuccessHandler jwtLogoutSuccessHandler(
            ObjectMapper objectMapper, AccountService accountService, JwtTokenProvider jwtTokenProvider) {
        return new JwtLogoutSuccessHandler(objectMapper);
    }

    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return new JwtAuthenticationEntryPoint(objectMapper);
    }
}
