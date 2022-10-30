package com.comeeatme.domain.favorite.repository;

import com.comeeatme.domain.favorite.Favorite;
import com.comeeatme.domain.favorite.FavoriteGroup;
import com.comeeatme.domain.member.Member;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

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

    @Override
    public Slice<Favorite> findSliceWithByMemberAndGroup(Pageable pageable, Member member, FavoriteGroup group) {
        List<Favorite> content = query
                .selectFrom(favorite)
                .join(favorite.restaurant).fetchJoin()
                .where(
                        favorite.member.eq(member),
                        favoriteGroupEq(group)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1L)
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    public BooleanExpression favoriteGroupEq(@Nullable FavoriteGroup group) {
        return Optional.ofNullable(group)
                .map(favorite.group::eq)
                .orElse(null);
    }

}
