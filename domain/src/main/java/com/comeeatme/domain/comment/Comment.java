package com.comeeatme.domain.comment;

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
@Table(name = "comment",
        indexes = {
        @Index(name = "IX_comment_member", columnList = "member_id"),
        @Index(name = "IX_comment_post", columnList = "post_id"),
        @Index(name = "IX_comment_parent", columnList = "parent_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Comment parent;

    @Column(name = "content", length = 1000, nullable = false)
    private String content;

    @Builder
    private Comment(
            Member member,
            Post post,
            @Nullable Comment parent,
            String content) {
        this.member = member;
        this.post = post;
        this.parent = parent;
        this.content = content;
    }

    public CommentEditor.CommentEditorBuilder toEditor() {
        return CommentEditor.builder()
                .content(content);
    }

    public void edit(CommentEditor editor) {
        content = editor.getContent();
    }
}
