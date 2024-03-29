package com.comeeatme.api.restaurant;

import com.comeeatme.api.exception.EntityNotFoundException;
import com.comeeatme.api.restaurant.request.RestaurantSearch;
import com.comeeatme.api.restaurant.response.RestaurantDto;
import com.comeeatme.domain.address.AddressCode;
import com.comeeatme.domain.address.repository.AddressCodeRepository;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
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
                        .postCount(restaurant.getPostCount())
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

    public RestaurantDto get(Long restaurantId) {
        Restaurant restaurant = getRestaurantById(restaurantId);
        Integer favoriteCount = restaurant.getFavoriteCount();
        return RestaurantDto.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .postCount(restaurant.getPostCount())
                .favoriteCount(favoriteCount)
                .addressName(restaurant.getAddress().getName())
                .addressRoadName(restaurant.getAddress().getRoadName())
                .build();
    }

    public Slice<RestaurantDto> getOrderedList(Pageable pageable, @Nullable String addressCodeCode) {
        Slice<Restaurant> restaurants = Optional.ofNullable(addressCodeCode)
                .map(code -> restaurantRepository
                        .findSliceByAddressAddressCodeCodeStartingWithAndPostCountGreaterThanAndUseYnIsTrue(
                                pageable, getAddressCodeByCode(code).getCodePrefix(), 0))
                .orElseGet(() -> restaurantRepository.findSliceByPostCountGreaterThanAndUseYnIsTrue(pageable, 0));
        return restaurants
                .map(restaurant -> RestaurantDto.builder()
                        .id(restaurant.getId())
                        .name(restaurant.getName())
                        .postCount(restaurant.getPostCount())
                        .favoriteCount(restaurant.getFavoriteCount())
                        .addressName(restaurant.getAddress().getName())
                        .addressRoadName(restaurant.getAddress().getRoadName())
                        .build()
                );
    }

    private Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .filter(Restaurant::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant.id=" + id));
    }

    private AddressCode getAddressCodeByCode(String code) {
        return addressCodeRepository.findById(code)
                .filter(AddressCode::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("AddressCode.code=" + code));
    }

}
