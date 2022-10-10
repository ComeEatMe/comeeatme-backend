package com.comeeatme.domain.post.repository;

import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.request.PostSearch;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Optional;

import static com.comeeatme.domain.account.QAccount.account;
import static com.comeeatme.domain.member.QMember.member;
import static com.comeeatme.domain.post.QPost.post;
import static com.comeeatme.domain.restaurant.QRestaurant.restaurant;

@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public boolean existsByIdAndUsernameAndUseYnIsTrue(Long postId, String username) {
        return Optional.ofNullable(query
                        .selectOne()
                        .from(post)
                        .join(post.member, member)
                        .where(
                                post.id.eq(postId),
                                post.useYn.isTrue(),
                                member.id.eq(memberIdOfUsername(username)))
                        .fetchOne())
                .isPresent();
    }

    private Expression<Long> memberIdOfUsername(String username) {
        return JPAExpressions
                .select(member.id)
                .from(account)
                .join(account.member, member)
                .where(account.username.eq(username));
    }

    @Override
    public Slice<Post> findAllWithMemberAndRestaurant(Pageable pageable, PostSearch postSearch) {
        List<Post> content = query
                .selectFrom(post)
                .join(post.member).fetchJoin()
                .join(post.restaurant, restaurant).fetchJoin()
                .where(
                        post.useYn.isTrue(),
                        restaurantIdEq(postSearch.getRestaurantId()
                        ))
                .orderBy(post.id.desc())
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

    private BooleanExpression restaurantIdEq(Long restaurantId) {
        return Optional.ofNullable(restaurantId)
                .map(restaurant.id::eq)
                .orElse(null);
    }
}
