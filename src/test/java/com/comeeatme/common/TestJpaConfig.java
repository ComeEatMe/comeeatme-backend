package com.comeeatme.common;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.persistence.EntityManager;
import java.util.Optional;

@TestConfiguration
@EnableJpaAuditing
@RequiredArgsConstructor
public class TestJpaConfig {

    private final EntityManager em;

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("test-writer");
    }

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(em);
    }
}
