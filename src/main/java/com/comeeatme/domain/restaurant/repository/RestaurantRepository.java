package com.comeeatme.domain.restaurant.repository;

import com.comeeatme.domain.restaurant.Restaurant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long>, RestaurantRepositoryCustom {

    Slice<Restaurant> findSliceByNameStartingWithAndUseYnIsTrue(Pageable pageable, String name);

    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<Restaurant> findWithPessimisticLockById(Long id);

}
