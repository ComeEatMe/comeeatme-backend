package com.comeeatme.security;

import com.comeeatme.domain.account.repository.AccountRepository;
import com.comeeatme.security.jwt.JwtAuthenticationCheckFilter;
import com.comeeatme.security.jwt.JwtLogoutHandler;
import com.comeeatme.security.jwt.JwtLogoutSuccessHandler;
import com.comeeatme.security.jwt.JwtTokenProvider;
import com.comeeatme.security.oauth2.OAuth2AuthenticationSuccessHandlerCustom;
import com.comeeatme.security.oauth2.OAuth2UserServiceCustom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] GET_PERMITTED_URLS = {
            "/code",
            "/docs/**"
            "/"
    };

    private static final String[] POST_PERMITTED_URLS = {
    };

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity,
            OAuth2UserServiceCustom oAuth2UserService,
            OAuth2AuthenticationSuccessHandlerCustom oAuth2AuthenticationSuccessHandler,
            JwtAuthenticationCheckFilter jwtAuthenticationCheckFilter,
            JwtLogoutHandler jwtLogoutHandler,
            JwtLogoutSuccessHandler jwtLogoutSuccessHandler)
            throws Exception {
        return httpSecurity
                .cors()
                .and()

                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                .formLogin().disable()
                .httpBasic().disable()

                .addFilterAfter(jwtAuthenticationCheckFilter, UsernamePasswordAuthenticationFilter.class)

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
        return web -> web.ignoring().antMatchers("/docs/**");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin(CorsConfiguration.ALL);
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
            @Value("${jwt.secret}") String secret,
            UserDetailsService userDetailsService) {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
                accessTokenValidity, refreshTokenValidity, secret, userDetailsService);
        return jwtTokenProvider;
    }

    @Bean
    public JwtAuthenticationCheckFilter jwtAuthenticationCheckFilter(JwtTokenProvider jwtTokenProvider) {
        JwtAuthenticationCheckFilter jwtAuthenticationCheckFilter = new JwtAuthenticationCheckFilter(jwtTokenProvider);
        return jwtAuthenticationCheckFilter;
    }

    @Bean
    public JwtLogoutHandler jwtLogoutHandler(JwtTokenProvider jwtTokenProvider, AccountRepository accountRepository) {
        JwtLogoutHandler jwtLogoutHandler = new JwtLogoutHandler(jwtTokenProvider, accountRepository);
        return jwtLogoutHandler;
    }

    @Bean
    public JwtLogoutSuccessHandler jwtLogoutSuccessHandler() {
        JwtLogoutSuccessHandler jwtLogoutSuccessHandler = new JwtLogoutSuccessHandler();
        return jwtLogoutSuccessHandler;
    }
}
