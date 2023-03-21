package com.comeeatme.domain.notice.repository;

import com.comeeatme.domain.common.TestJpaConfig;
import com.comeeatme.domain.notice.Notice;
import com.comeeatme.domain.notice.NoticeType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestJpaConfig.class)
@DataJpaTest
@Transactional
class NoticeRepositoryTest {

    @Autowired
    private NoticeRepository noticeRepository;

    @Test
    void findSliceByUseYnIsTrue() {
        // given
        List<Notice> notices = noticeRepository.saveAllAndFlush(List.of(
                Notice.builder()
                        .type(NoticeType.NOTICE)
                        .title("title-1")
                        .content("content-1")
                        .build(),
                Notice.builder()
                        .type(NoticeType.EVENT)
                        .title("title-2")
                        .content("content-2")
                        .build()
        ));
        notices.get(0).delete();

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Notice> result = noticeRepository.findSliceByUseYnIsTrue(pageRequest).getContent();

        // then
        assertThat(result)
                .hasSize(1)
                .extracting("id").containsOnly(notices.get(1).getId());
    }

}