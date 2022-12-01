package com.comeeatme.domain.report;

import com.comeeatme.domain.common.core.BaseTimeEntity;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.post.Post;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.persistence.*;

@Entity
@Table(name = "report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", length = 45, nullable = false)
    private ReportReason reason;

    @Builder
    private Report(@Nullable Long id, Member member, Post post, ReportReason reason) {
        this.id = id;
        this.member = member;
        this.post = post;
        this.reason = reason;
    }
}
