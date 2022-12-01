package com.comeeatme.domain.restaurant.repository;

import com.comeeatme.domain.address.AddressCode;
import com.comeeatme.domain.restaurant.Restaurant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import javax.annotation.Nullable;
import java.util.List;

public interface RestaurantRepositoryCustom {

    Slice<Restaurant> findSliceByNameAndAddressCodesAndUseYnIsTrue(
            Pageable pageable, String name, @Nullable List<AddressCode> addressCodes);

    Slice<Restaurant> findSliceByNameAddressCodesStartingWithAndUseYnIsTrue(
            Pageable pageable, String name, @Nullable List<String> addressCodePrefixes);

}
