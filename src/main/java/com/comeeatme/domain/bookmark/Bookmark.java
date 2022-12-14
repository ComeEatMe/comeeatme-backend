package com.comeeatme.domain.bookmark;

import com.comeeatme.domain.common.core.BaseCreatedAtEntity;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.post.Post;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "bookmark",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_bookmark_post_member",
                        columnNames = {"post_id", "member_id"})
        },
        indexes = {
                @Index(name = "IX_bookmark_member", columnList = "member_id"),
                @Index(name = "IX_bookmark_post", columnList = "post_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookmark extends BaseCreatedAtEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Post post;

    @Builder
    private Bookmark(Member member, Post post) {
        this.member = member;
        this.post = post;
    }
}
