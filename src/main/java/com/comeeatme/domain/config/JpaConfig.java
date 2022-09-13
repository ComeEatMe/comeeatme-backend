package com.comeeatme.domain.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.EntityManager;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Configuration
@EnableJpaAuditing
@RequiredArgsConstructor
public class JpaConfig {

    private final EntityManager em;

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(
                nonNull(SecurityContextHolder.getContext().getAuthentication()) &&
                        SecurityContextHolder.getContext().getAuthentication().isAuthenticated() ?
                        SecurityContextHolder.getContext().getAuthentication().getName() : "Anonymous"
        );
    }

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(em);
    }
}
