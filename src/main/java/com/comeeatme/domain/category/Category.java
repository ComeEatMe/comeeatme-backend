package com.comeeatme.domain.category;

import com.comeeatme.domain.core.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category",
        uniqueConstraints =
        @UniqueConstraint(name = "UK_category_parent_name", columnNames = {"parent_id", "name"}),
        indexes = {
        @Index(name = "IX_category_name", columnList = "name"),
        @Index(name = "IX_category_parent_id", columnList = "parent_id")}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    /**
     * 최상위인 경우 null
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> children = new ArrayList<>();

    @Column(name = "name", length = 15, nullable = false)
    private String name;

    @Column(name = "full_name", length = 45, nullable = false)
    private String fullName;

    @Column(name = "depth", nullable = false)
    private Integer depth;

    public void addChild(Category child) {
        this.children.add(child);
        child.parent = this;
    }

    @Builder
    private Category(
            @Nullable Long id,
            @Nullable Category parent,
            String name,
            String fullName,
            Integer depth) {
        this.id = id;
        this.parent = parent;
        this.name = name;
        this.fullName = fullName;
        this.depth = depth;
    }
}
