package com.comeeatme.domain.post;

import com.comeeatme.domain.image.Image;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.persistence.*;

@Entity
@Table(name = "post_image",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_post_image_image", columnNames = "image_id"),
        },
        indexes = {
                @Index(name = "IX_post_image_post", columnList = "post_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Post post;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Image image;

    @Builder
    private PostImage(
            @Nullable Long id,
            Post post,
            Image image) {
        this.id = id;
        this.post = post;
        this.image = image;
    }
}
