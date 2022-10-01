package com.comeeatme.domain.post;

import com.comeeatme.domain.core.BaseTimeEntity;
import com.comeeatme.domain.restaurant.Restaurant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "post", indexes = {
        @Index(name = "IX_post_restaurant_id", columnList = "restaurant_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Restaurant restaurant;

    @ElementCollection
    @CollectionTable(name = "post_hashtag",
            joinColumns = @JoinColumn(name = "post_id", foreignKey = @ForeignKey(name = "FK_post_hashtag_post_id")))
    @Enumerated(EnumType.STRING)
    @Column(name = "hashtag", length = 25, nullable = false)
    private Set<HashTag> hashTags;

    @OneToMany(mappedBy = "image")
    private List<PostImage> postImages;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Builder
    private Post(
            @Nullable Long id,
            Restaurant restaurant,
            @Nullable Set<HashTag> hashTags,
            @Nullable List<PostImage> postImages,
            String content) {
        this.id = id;
        this.restaurant = restaurant;
        this.hashTags = Optional.ofNullable(hashTags).orElse(new HashSet<>());
        this.postImages = Optional.ofNullable(postImages).orElse(new ArrayList<>());
        this.content = content;
    }
}
