package com.comeeatme.domain.post;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "post_hashtag",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_post_hashtag_post_hashtag", columnNames = {"post_id", "hashtag"})
        },
        indexes = {
                @Index(name = "IX_post_hashtag_post", columnList = "post_id"),
                @Index(name = "IX_post_hashtag_hashtag", columnList = "hashtag")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostHashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_hashtag_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(name = "hashtag", length = 45, nullable = false)
    private Hashtag hashtag;

    @Builder
    private PostHashtag(@Nullable Long id, Post post, Hashtag hashtag) {
        this.id = id;
        this.post = post;
        this.hashtag = hashtag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostHashtag that = (PostHashtag) o;
        return getPost().getId().equals(that.getPost().getId())
                && getHashtag() == that.getHashtag();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPost().getId(), getHashtag());
    }
}
