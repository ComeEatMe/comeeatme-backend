package com.comeeatme.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Aspect
@Slf4j
@Configuration
public class ClientLogConfig {

    @Around("@within(com.comeeatme.batch.config.ClientLog)")
    public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();
        log.info("호출 시작 {} >> {}", joinPoint.getSignature(), Arrays.toString(joinPoint.getArgs()));
        Object result = joinPoint.proceed();

        long time = System.currentTimeMillis() - start;
        log.info("호출 완료 {} ({}) >> {}", joinPoint.getSignature(), time, result);

        return result;
    }
}
