package com.comeeatme.api.report;

import com.comeeatme.api.common.response.CreateResult;
import com.comeeatme.api.exception.EntityNotFoundException;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.report.Report;
import com.comeeatme.domain.report.ReportReason;
import com.comeeatme.domain.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    private final PostRepository postRepository;

    private final MemberRepository memberRepository;

    @Transactional
    public CreateResult<Long> report(Long postId, ReportReason reason, Long memberId) {
        Post post = getPostById(postId);
        Member member = getMemberById(memberId);
        Report report = reportRepository.save(
                Report.builder()
                        .member(member)
                        .post(post)
                        .reason(reason)
                        .build()
        );
        return new CreateResult<>(report.getPost().getId());
    }

    private Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .filter(Post::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Post id=" + postId));
    }

    private Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .filter(Member::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Member.id=" + id));
    }

}
