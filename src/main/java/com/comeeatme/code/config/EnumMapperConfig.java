package com.comeeatme.code.config;

import com.comeeatme.domain.post.Hashtag;
import com.comeeatme.domain.post.HashtagGroup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnumMapperConfig {

    /**
     *  아래와 같이 EnumMapperType 을 구현한 enum 을 enumMapperFactory 에 등록합니다.
     *  enumMapperFactory.put(EnumCodeExample 을 등록할 이름, EnumCodeExample.class);
     */
    @Bean
    public EnumMapperFactory enumMapperFactory() {
        EnumMapperFactory enumMapperFactory = new EnumMapperFactory();
        enumMapperFactory.put("HashtagGroup", HashtagGroup.class);
        enumMapperFactory.put("Hashtag", Hashtag.class);
        return enumMapperFactory;
    }
}
