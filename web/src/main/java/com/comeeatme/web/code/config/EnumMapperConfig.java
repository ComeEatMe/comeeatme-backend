package com.comeeatme.web.code.config;

import com.comeeatme.domain.member.MemberDeleteReason;
import com.comeeatme.domain.notice.NoticeType;
import com.comeeatme.domain.post.Hashtag;
import com.comeeatme.domain.post.HashtagGroup;
import com.comeeatme.domain.report.ReportReason;
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
        enumMapperFactory.put("ReportReason", ReportReason.class);
        enumMapperFactory.put("NoticeType", NoticeType.class);
        enumMapperFactory.put("MemberDeleteReason", MemberDeleteReason.class);
        return enumMapperFactory;
    }
}
