package com.comeeatme.domain.post;

import com.comeeatme.domain.common.core.BaseTimeEntity;
import com.comeeatme.domain.images.Images;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.persistence.*;

@Entity
@Table(name = "post_image",
        indexes = {
                @Index(name = "IX_post_image_post_id", columnList = "post_id"),
                @Index(name = "IX_post_image_image_id", columnList = "image_id")}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Post post;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Images image;

    @Builder
    private PostImage(
            @Nullable Long id,
            Post post,
            Images image) {
        this.id = id;
        this.post = post;
        this.image = image;
    }
}
