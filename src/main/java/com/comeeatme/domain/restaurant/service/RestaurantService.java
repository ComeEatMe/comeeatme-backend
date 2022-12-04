package com.comeeatme.domain.restaurant.service;

import com.comeeatme.domain.address.AddressCode;
import com.comeeatme.domain.address.repository.AddressCodeRepository;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import com.comeeatme.domain.restaurant.request.RestaurantSearch;
import com.comeeatme.domain.restaurant.response.RestaurantDetailDto;
import com.comeeatme.domain.restaurant.response.RestaurantDto;
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    private final AddressCodeRepository addressCodeRepository;

    public Slice<RestaurantDto> search(Pageable pageable, RestaurantSearch restaurantSearch) {
        Deque<String> querySplit = new LinkedList<>(Arrays.asList(restaurantSearch.getKeyword().split(" ")));
        List<AddressCode> addressCodesInQuery = getAddressCodesInQuerySplit(querySplit);
        List<String> addressCodePrefixes = addressCodesInQuery.stream()
                .map(AddressCode::getCodePrefix)
                .collect(Collectors.toList());
        addressCodePrefixes = addressCodePrefixes.isEmpty() ? null : addressCodePrefixes;

        String name = String.join(" ", querySplit);

        Slice<Restaurant> restaurants = restaurantRepository.findSliceByNameAddressCodesStartingWithAndUseYnIsTrue(
                pageable, name, addressCodePrefixes);
        return restaurants
                .map(restaurant -> RestaurantDto.builder()
                        .id(restaurant.getId())
                        .name(restaurant.getName())
                        .favoriteCount(restaurant.getFavoriteCount())
                        .addressName(restaurant.getAddress().getName())
                        .addressRoadName(restaurant.getAddress().getRoadName())
                        .build()
                );
    }

    private List<AddressCode> getAddressCodesInQuerySplit(Deque<String> querySplit) {
        if (querySplit.isEmpty()) {
            return Collections.emptyList();
        }

        List<AddressCode> addressCodes = new ArrayList<>();

        List<AddressCode> firstCodes = addressCodeRepository.findAllByNameStartingWith(querySplit.peekFirst())
                .stream()
                .filter(AddressCode::getUseYn)
                .collect(Collectors.toList());
        if (!firstCodes.isEmpty()) {
            querySplit.pollFirst();
            addressCodes.addAll(firstCodes);
        }

        List<AddressCode> lastCodes = addressCodeRepository.findAllByNameStartingWith(querySplit.peekLast())
                .stream()
                .filter(AddressCode::getUseYn)
                .collect(Collectors.toList());
        if (!lastCodes.isEmpty()) {
            querySplit.pollLast();
            addressCodes.addAll(lastCodes);
        }

        return addressCodes;
    }

    public RestaurantDetailDto get(Long restaurantId) {
        Restaurant restaurant = getRestaurantById(restaurantId);
        Integer favoriteCount = restaurant.getFavoriteCount();
        return RestaurantDetailDto.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .favoriteCount(favoriteCount)
                .addressName(restaurant.getAddress().getName())
                .addressRoadName(restaurant.getAddress().getRoadName())
                .build();
    }

    private Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .filter(Restaurant::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant.id=" + id));
    }

}
