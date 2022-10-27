package com.comeeatme.domain.image;

import com.comeeatme.domain.common.core.BaseTimeEntity;
import com.comeeatme.domain.member.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.persistence.*;

@Entity
@Table(name = "images",
        indexes = {
                @Index(name = "IX_images_member_id", columnList = "member_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "images_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    @Column(name = "origin_name", nullable = false)
    private String originName;

    @Column(name = "stored_name", nullable = false)
    private String storedName;

    @Column(name = "url", nullable = false)
    private String url;

    @Builder
    private Image(
            @Nullable Long id,
            Member member,
            String originName,
            String storedName,
            String url) {
        this.id = id;
        this.member = member;
        this.originName = originName;
        this.storedName = storedName;
        this.url = url;
    }
}
