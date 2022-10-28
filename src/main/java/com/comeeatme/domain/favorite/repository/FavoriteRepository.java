package com.comeeatme.domain.favorite.repository;

import com.comeeatme.domain.favorite.Favorite;
import com.comeeatme.domain.favorite.FavoriteGroup;
import com.comeeatme.domain.restaurant.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByGroupAndRestaurant(FavoriteGroup group, Restaurant restaurant);

    Optional<Favorite> findByGroupAndRestaurant(FavoriteGroup group, Restaurant restaurant);

}
