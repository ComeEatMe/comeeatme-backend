package com.comeeatme.domain.favorite.repository;

import com.comeeatme.domain.favorite.Favorite;
import com.comeeatme.domain.favorite.FavoriteGroup;
import com.comeeatme.domain.member.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface FavoriteRepositoryCustom {

    List<Favorite> findAllByMemberIdAndRestaurantIds(Long memberId, List<Long> postIds);

    Slice<Favorite> findSliceWithByMemberAndGroup(Pageable pageable, Member member, FavoriteGroup group);

}
