package com.comeeatme.domain.report;

import com.comeeatme.domain.common.TestJpaConfig;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.report.repository.ReportRepository;
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
class ReportTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("Report 생성 및 저장")
    void save() {
        assertThatNoException().isThrownBy(() -> reportRepository.save(
                Report.builder()
                        .member(memberRepository.getReferenceById(3L))
                        .post(postRepository.getReferenceById(2L))
                        .reason(ReportReason.SPAM)
                        .build()
        ));

    }

}