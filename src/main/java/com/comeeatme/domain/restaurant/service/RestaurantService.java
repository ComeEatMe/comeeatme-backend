package com.comeeatme.domain.restaurant.service;

import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import com.comeeatme.domain.restaurant.response.RestaurantDetailDto;
import com.comeeatme.domain.restaurant.response.RestaurantSimpleDto;
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public Slice<RestaurantSimpleDto> getSimpleList(Pageable pageable, String name) {
        return restaurantRepository.findSliceByNameStartingWithAndUseYnIsTrue(pageable, name)
                .map(restaurant -> RestaurantSimpleDto.builder()
                        .id(restaurant.getId())
                        .name(restaurant.getName())
                        .addressName(restaurant.getAddress().getName())
                        .build()
                );
    }

    public RestaurantDetailDto get(Long restaurantId) {
        Restaurant restaurant = getRestaurantById(restaurantId);
        return RestaurantDetailDto.of(restaurant);
    }

    private Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .filter(Restaurant::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant.id=" + id));
    }

}
