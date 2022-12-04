package com.comeeatme.domain.post;

import com.comeeatme.domain.common.core.BaseTimeEntity;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.restaurant.Restaurant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "post",
        indexes = {
        @Index(name = "IX_post_member", columnList = "member_id"),
        @Index(name = "IX_post_restaurant", columnList = "restaurant_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Restaurant restaurant;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostHashtag> postHashtags;

    @Column(name = "content", length = 2000, nullable = false)
    private String content;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount;

    @Column(name = "bookmark_count", nullable = false)
    private Integer bookmarkCount;

    @Builder
    private Post(
            @Nullable Long id,
            Member member,
            Restaurant restaurant,
            @Nullable Set<PostHashtag> postHashtags,
            String content) {
        this.id = id;
        this.member = member;
        this.restaurant = restaurant;
        this.postHashtags = Optional.ofNullable(postHashtags).orElse(new HashSet<>());
        this.content = content;
        this.commentCount = 0;
        this.likeCount = 0;
        this.bookmarkCount = 0;
    }

    public PostEditor.PostEditorBuilder toEditor() {
        return PostEditor.builder()
                .restaurant(restaurant)
                .content(content);
    }

    public void edit(PostEditor editor) {
        restaurant = editor.getRestaurant();
        content = editor.getContent();
    }

    public void addHashtag(Hashtag hashtag) {
        PostHashtag postHashtag = PostHashtag.builder()
                .post(this)
                .hashtag(hashtag)
                .build();
        postHashtags.add(postHashtag);
    }

    public List<Hashtag> getHashtags() {
        return postHashtags.stream()
                .map(PostHashtag::getHashtag)
                .collect(Collectors.toList());
    }

    public void increaseCommentCount() {
        commentCount += 1;
    }

    public void decreaseCommentCount() {
        if (commentCount < 1) {
            throw new IllegalStateException("commentCount가 더 이상 감소될 수 없습니다.");
        }
        commentCount -= 1;
    }

    public void increaseLikeCount() {
        likeCount += 1;
    }

    public void decreaseLikeCount() {
        if (likeCount < 1) {
            throw new IllegalStateException("likeCount가 더 이상 감소될 수 없습니다.");
        }
        likeCount -= 1;
    }

    public void increaseBookmarkCount() {
        bookmarkCount += 1;
    }

    public void decreaseBookmarkCount() {
        if (bookmarkCount < 1) {
            throw new IllegalStateException("bookmarkCount가 더 이상 감소될 수 없습니다.");
        }
        bookmarkCount -= 1;
    }

}
