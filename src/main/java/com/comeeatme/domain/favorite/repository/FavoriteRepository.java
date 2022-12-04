package com.comeeatme.domain.favorite.repository;

import com.comeeatme.domain.favorite.Favorite;
import com.comeeatme.domain.favorite.FavoriteGroup;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.restaurant.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long>, FavoriteRepositoryCustom {

    boolean existsByMemberAndGroupAndRestaurant(Member member, FavoriteGroup group, Restaurant restaurant);

    Optional<Favorite> findByMemberAndGroupAndRestaurant(Member member, FavoriteGroup group, Restaurant restaurant);

    long countByMember(Member member);

    boolean existsByMemberAndRestaurant(Member member, Restaurant restaurant);

}
