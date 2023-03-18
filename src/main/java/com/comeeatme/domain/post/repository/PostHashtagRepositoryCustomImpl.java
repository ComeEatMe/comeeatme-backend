package com.comeeatme.domain.post.repository;

import com.comeeatme.domain.post.Hashtag;
import com.comeeatme.domain.post.response.QRestaurantHashtag;
import com.comeeatme.domain.post.response.RestaurantHashtag;
import com.comeeatme.domain.restaurant.Restaurant;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.comeeatme.domain.post.QPost.post;
import static com.comeeatme.domain.post.QPostHashtag.postHashtag;

@RequiredArgsConstructor
public class PostHashtagRepositoryCustomImpl implements PostHashtagRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public List<Hashtag> findHashtagsByRestaurant(Restaurant restaurant) {
        return query
                .select(postHashtag.hashtag).distinct()
                .from(postHashtag)
                .join(postHashtag.post, post)
                .where(
                        post.restaurant.eq(restaurant),
                        post.useYn.isTrue()
                ).fetch();
    }

    @Override
    public List<RestaurantHashtag> findHashtagsByRestaurants(List<Restaurant> restaurants) {
        return query
                .select(new QRestaurantHashtag(post.restaurant.id, postHashtag.hashtag))
                .from(postHashtag)
                .join(postHashtag.post, post)
                .where(
                        post.restaurant.in(restaurants),
                        post.useYn.isTrue()
                ).groupBy(post.restaurant.id, postHashtag.hashtag)
                .fetch();
    }
}
