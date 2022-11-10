package com.comeeatme.domain.restaurant.repository;

import com.comeeatme.domain.restaurant.Address;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.request.RestaurantSearch;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
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

    private final EntityManager em;

    @Override
    public Slice<Restaurant> findSliceBySearchAndUseYnIsTrue(Pageable pageable, RestaurantSearch search) {
        StringBuilder sqlBuilder = new StringBuilder("select" +
                " r.restaurant_id" +
                ", r.name" +
                ", r.phone" +
                ", r.address_name" +
                ", r.road_address_name" +
                ", r.location" +
                ", r.use_yn" +
                ", r.created_at" +
                ", r.last_modified_at" +
                " from restaurant r" +
                " where");

        whereCond(search, sqlBuilder);
        orderbyCond(search, sqlBuilder);

        String sql = sqlBuilder.toString();
        Query contentQuery = em.createNativeQuery(sql, Restaurant.class);
        setParameter(contentQuery, search);
        List content = contentQuery
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize() + 1)
                .getResultList();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private void setParameter( Query query, RestaurantSearch search) {
        if (nonNull(search.getName())) {
            query.setParameter("name", (search.getName() + "%"));
        }
        if (nonNull(search.getX()) && nonNull(search.getY())) {
            Point curLoc = Address.createPoint(search.getX(), search.getY());
            query.setParameter("curLoc", curLoc);
        }
        if (nonNull(search.getDistance())) {
            query.setParameter("distance", search.getDistance());
        }
    }

    private void orderbyCond(RestaurantSearch search, StringBuilder sqlBuilder) {
        if (nonNull(search.getX()) && nonNull(search.getY())) {
            sqlBuilder.append(" order by st_distance_sphere(" +
                    "r.location, :curLoc)");
        }
    }

    private void whereCond(RestaurantSearch search, StringBuilder sqlBuilder) {
        if (nonNull(search.getName())) {
            sqlBuilder.append(" r.name like :name and");
        }
        if (nonNull(search.getDistance()) && nonNull(search.getX()) && nonNull(search.getY())) {
            sqlBuilder.append(
                    " :distance >= st_distance_sphere(" +
                            "r.location, :curLoc) and"
            );
        }
        sqlBuilder.append(" r.use_yn=true");
    }

}
