package com.comeeatme.web.common.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@Configuration
@EntityScan("com.comeeatme.domain")
@EnableJpaRepositories("com.comeeatme.domain")
@ComponentScan(value = {"com.comeeatme.api", "com.comeeatme.domain"})
public class AppConfig {

    @PostConstruct
    void postConstruct() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
