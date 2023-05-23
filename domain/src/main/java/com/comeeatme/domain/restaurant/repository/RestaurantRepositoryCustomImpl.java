package com.comeeatme.domain.restaurant.repository;

import com.comeeatme.domain.address.AddressCode;
import com.comeeatme.domain.restaurant.Restaurant;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static com.comeeatme.domain.restaurant.QRestaurant.restaurant;

@RequiredArgsConstructor
public class RestaurantRepositoryCustomImpl implements RestaurantRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public Slice<Restaurant> findSliceByNameAndAddressCodesAndUseYnIsTrue(
            Pageable pageable, String name, @Nullable List<AddressCode> addressCodes) {
        List<Restaurant> content = query
                .selectFrom(restaurant)
                .where(
                        restaurant.name.startsWith(name),
                        addressCodeIn(addressCodes),
                        restaurant.useYn.isTrue()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1L)
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private BooleanExpression addressCodeIn(@Nullable List<AddressCode> addressCodes) {
        return Optional.ofNullable(addressCodes)
                .map(restaurant.address.addressCode::in)
                .orElse(null);
    }

    @Override
    public Slice<Restaurant> findSliceByNameAddressCodesStartingWithAndUseYnIsTrue(
            Pageable pageable, String name, @Nullable List<String> addressCodePrefixes) {
        List<Restaurant> content = query
                .selectFrom(restaurant)
                .where(
                        restaurant.name.startsWith(name),
                        addressCodesStartingWith(addressCodePrefixes),
                        restaurant.useYn.isTrue()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1L)
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private BooleanExpression addressCodesStartingWith(@Nullable List<String> addressCodePrefixes) {
        if (addressCodePrefixes == null) {
            return null;
        }
        return addressCodePrefixes.stream()
                .map(restaurant.address.addressCode.code::startsWith)
                .reduce(BooleanExpression::or)
                .orElse(null);
    }
}
