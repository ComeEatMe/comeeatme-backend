package com.comeeatme.logger;

import com.comeeatme.logger.trace.logtrace.ThreadLocalLogTrace;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AopLoggerConfig {

    @Bean
    public LogTraceAspect logTraceAspect() {
        return new LogTraceAspect(new ThreadLocalLogTrace());
    }
}
