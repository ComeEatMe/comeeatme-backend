package com.comeeatme.domain.report.service;

import com.comeeatme.domain.common.response.CreateResult;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.report.Report;
import com.comeeatme.domain.report.ReportReason;
import com.comeeatme.domain.report.repository.ReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @Test
    void report() {
        // given
        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(post.getId()).willReturn(1L);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(2L)).willReturn(Optional.of(member));

        Report report = mock(Report.class);
        given(report.getPost()).willReturn(post);
        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        given(reportRepository.save(reportCaptor.capture())).willReturn(report);

        // when
        CreateResult<Long> result = reportService.report(1L, ReportReason.DUPLICATE, 2L);

        // then
        Report captorValue = reportCaptor.getValue();
        assertThat(captorValue.getMember()).isEqualTo(member);
        assertThat(captorValue.getPost()).isEqualTo(post);
        assertThat(captorValue.getReason()).isEqualTo(ReportReason.DUPLICATE);
        assertThat(result.getId()).isEqualTo(1L);
    }
}