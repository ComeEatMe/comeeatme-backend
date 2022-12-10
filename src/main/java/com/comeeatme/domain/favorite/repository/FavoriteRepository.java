package com.comeeatme.domain.favorite.repository;

import com.comeeatme.domain.favorite.Favorite;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.restaurant.Restaurant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long>, FavoriteRepositoryCustom {

    Optional<Favorite> findByRestaurantAndMember(Restaurant restaurant, Member member);

    long countByMember(Member member);

    boolean existsByRestaurantAndMember(Restaurant restaurant, Member member);

    @EntityGraph(attributePaths = "restaurant")
    Slice<Favorite> findSliceWithRestaurantByMember(Pageable pageable, Member member);

    List<Favorite> findAllByMember(Member member);

}
