package com.comeeatme.domain.likes;

import com.comeeatme.domain.common.core.BaseCreatedAtEntity;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.post.Post;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.persistence.*;

@Entity
@Table(name = "likes",
        uniqueConstraints = {
            @UniqueConstraint(name = "UK_likes_member_post", columnNames = {"post_id", "member_id"})
        },
        indexes = {
                @Index(name = "IX_likes_post_id", columnList = "post_id"),
                @Index(name = "IX_likes_member_id", columnList = "member_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Likes extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "likes_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    @Builder
    private Likes(@Nullable Long id, Post post, Member member) {
        this.id = id;
        this.post = post;
        this.member = member;
    }
}
