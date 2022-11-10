package com.comeeatme.domain.favorite.repository;

import com.comeeatme.domain.favorite.Favorite;
import com.comeeatme.domain.favorite.FavoriteGroup;
import com.comeeatme.domain.favorite.response.FavoriteCount;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.restaurant.Restaurant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface FavoriteRepositoryCustom {

    List<Favorite> findAllByMemberIdAndRestaurantIds(Long memberId, List<Long> postIds);

    Slice<Favorite> findSliceWithByMemberAndGroup(Pageable pageable, Member member, FavoriteGroup group);

    List<FavoriteCount> countsGroupByRestaurants(List<Restaurant> restaurants);

}
