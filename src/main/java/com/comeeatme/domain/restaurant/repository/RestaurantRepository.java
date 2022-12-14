package com.comeeatme.domain.restaurant.repository;

import com.comeeatme.domain.restaurant.Restaurant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long>, RestaurantRepositoryCustom {

    Slice<Restaurant> findSliceByNameStartingWithAndUseYnIsTrue(Pageable pageable, String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Restaurant> findWithPessimisticLockById(Long id);

    Slice<Restaurant> findSliceByPostCountGreaterThanAndUseYnIsTrue(Pageable pageable, int postCount);

    Slice<Restaurant> findSliceByAddressAddressCodeCodeStartingWithAndPostCountGreaterThanAndUseYnIsTrue(
            Pageable pageable, String addressCode, int postCount);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Restaurant> findAllWithPessimisticLockByIdIn(Collection<Long> id);

}
