package com.comeeatme.domain.restaurant.repository;

import com.comeeatme.domain.restaurant.Address;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.request.RestaurantSearch;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor
public class RestaurantRepositoryCustomImpl implements RestaurantRepositoryCustom {

    private final JPAQueryFactory query;

}
