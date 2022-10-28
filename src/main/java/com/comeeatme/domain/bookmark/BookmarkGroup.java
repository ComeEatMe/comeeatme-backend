package com.comeeatme.domain.bookmark;

import com.comeeatme.domain.common.core.BaseCreatedAtEntity;
import com.comeeatme.domain.member.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.persistence.*;

@Entity
@Table(name = "bookmark_group",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_bookmark_group_member_name", columnNames = {"member_id", "name"})
        },
        indexes = {
                @Index(name = "IX_bookmark_group_member", columnList = "member_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookmarkGroup extends BaseCreatedAtEntity {

    public static final String ALL_NAME = "ALL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_group_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    @Column(name = "name", length = 25, nullable = false)
    private String name;

    @Column(name = "bookmark_count", nullable = false)
    private Integer bookmarkCount;

    @Builder
    private BookmarkGroup(@Nullable Long id, Member member, String name, Integer bookmarkCount) {
        this.id = id;
        this.member = member;
        this.name = name;
        this.bookmarkCount = bookmarkCount;
    }
}
