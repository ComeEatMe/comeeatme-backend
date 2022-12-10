package com.comeeatme.domain.favorite.repository;

import com.comeeatme.domain.favorite.Favorite;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.comeeatme.domain.favorite.QFavorite.favorite;

@RequiredArgsConstructor
public class FavoriteRepositoryCustomImpl implements FavoriteRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public List<Favorite> findAllByMemberIdAndRestaurantIds(Long memberId, List<Long> restaurantIds) {
        return query
                .selectFrom(favorite)
                .where(
                        favorite.member.id.eq(memberId),
                        favorite.restaurant.id.in(restaurantIds)
                ).fetch();
    }

}
