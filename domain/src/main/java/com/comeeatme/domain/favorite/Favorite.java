package com.comeeatme.domain.favorite;

import com.comeeatme.domain.common.core.BaseCreatedAtEntity;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.restaurant.Restaurant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "favorite",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_favorite_restaurant_member",
                        columnNames = {"restaurant_id", "member_id"})
        },
        indexes = {
                @Index(name = "IX_favorite_member", columnList = "member_id"),
                @Index(name = "IX_favorite_restaurant", columnList = "restaurant_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Restaurant restaurant;

    @Builder
    private Favorite(Member member, Restaurant restaurant) {
        this.member = member;
        this.restaurant = restaurant;
    }

}
