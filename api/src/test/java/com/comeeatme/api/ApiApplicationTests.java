package com.comeeatme.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootTest
@SpringBootApplication
@EntityScan("com.comeeatme.domain")
@EnableJpaRepositories("com.comeeatme.domain")
@ComponentScan({"com.comeeatme.domain", "com.comeeatme.api"})
class ApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
