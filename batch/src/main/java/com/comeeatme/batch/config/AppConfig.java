package com.comeeatme.batch.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan("com.comeeatme.domain")
@EnableJpaRepositories("com.comeeatme.domain")
@ComponentScan(value = {"com.comeeatme.batch", "com.comeeatme.domain"})
@Configuration
public class AppConfig {
}
