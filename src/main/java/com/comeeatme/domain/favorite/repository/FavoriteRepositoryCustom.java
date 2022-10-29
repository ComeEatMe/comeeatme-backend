package com.comeeatme.domain.favorite.repository;

import com.comeeatme.domain.favorite.Favorite;

import java.util.List;

public interface FavoriteRepositoryCustom {

    List<Favorite> findAllByMemberIdAndRestaurantIds(Long memberId, List<Long> postIds);

}
