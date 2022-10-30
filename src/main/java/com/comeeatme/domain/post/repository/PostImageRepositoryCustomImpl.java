package com.comeeatme.domain.post.repository;

import com.comeeatme.domain.post.PostImage;
import com.comeeatme.domain.restaurant.Restaurant;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static com.comeeatme.domain.post.QPost.post;
import static com.comeeatme.domain.post.QPostImage.postImage;

@RequiredArgsConstructor
public class PostImageRepositoryCustomImpl implements PostImageRepositoryCustom {

    private final JPAQueryFactory query;

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

}
