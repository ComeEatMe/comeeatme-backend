package com.comeeatme.domain.post.repository;

import com.comeeatme.domain.post.PostImage;
import com.comeeatme.domain.post.response.RestaurantPostImage;
import com.comeeatme.domain.restaurant.Restaurant;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

import static com.comeeatme.domain.post.QPost.post;
import static com.comeeatme.domain.post.QPostImage.postImage;

@RequiredArgsConstructor
public class PostImageRepositoryCustomImpl implements PostImageRepositoryCustom {

    private final JPAQueryFactory query;

    private final EntityManager em;

    @Override
    public Slice<PostImage> findSliceWithImageByRestaurantAndUseYnIsTrue(Restaurant restaurant, Pageable pageable) {
        List<PostImage> content = query
                .selectFrom(postImage)
                .join(postImage.image).fetchJoin()
                .join(postImage.post, post)
                .where(
                        post.restaurant.eq(restaurant),
                        postImage.image.useYn.isTrue()
                ).offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1L)
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public List<RestaurantPostImage> findImagesByRestaurantsAndPostUseYnIsTrue(List<Restaurant> restaurants, int perImageNum) {
        StringBuilder builder = new StringBuilder(
                "select p.restaurant_id, pi.post_image_id\n" +
                        "from post_image pi\n" +
                        "       inner join post p on p.post_id = pi.post_id\n");
        for (int i = 0; i < restaurants.size(); i++) {
            Restaurant restaurant = restaurants.get(i);
            if (i == 0) {
                builder.append("where ");
            } else {
                builder.append("or ");
            }

            builder.append("pi.post_image_id in (" +
                    "select pi.post_image_id\n" +
                    "from post_image pi\n" +
                    "       inner join\n" +
                    "   (" +
                    "   select pi.post_image_id\n" +
                    "   from post_image pi\n" +
                    "           inner join post p on pi.post_id = p.post_id\n" +
                    "   where p.restaurant_id = " + restaurant.getId() + "\n" +
                    "    and p.use_yn = true\n" +
                    "   limit " + perImageNum +
                    "   ) ri on pi.post_image_id = ri.post_image_id" +
                    ")");
        }

        String sql = builder.toString();
        List<Object[]> resultList = em.createNativeQuery(sql)
                .getResultList();
        return resultList
                .stream()
                .map(row -> {
                    Long restaurantId = Long.parseLong(row[0].toString());
                    Long postImageId = Long.parseLong(row[1].toString());
                    return new RestaurantPostImage(restaurantId, postImageId);
                })
                .collect(Collectors.toList());
    }

}
