package com.comeeatme.domain.notice;

import com.comeeatme.domain.common.TestJpaConfig;
import com.comeeatme.domain.notice.repository.NoticeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatNoException;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class NoticeTest {

    @Autowired
    private NoticeRepository noticeRepository;

    @Test
    @DisplayName("Notice 생성 및 저장")
    void createAndSave() {
        assertThatNoException().isThrownBy(() -> noticeRepository.saveAndFlush(Notice.builder()
                .type(NoticeType.NOTICE)
                .title("제목")
                .content("공지사항")
                .build()
        ));
    }
}