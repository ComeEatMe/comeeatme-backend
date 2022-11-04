package com.comeeatme.domain.post.repository;

import com.comeeatme.domain.post.Hashtag;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.request.PostSearch;
import com.comeeatme.domain.restaurant.Restaurant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface PostRepositoryCustom {

    boolean existsByIdAndUsernameAndUseYnIsTrue(Long postId, String username);

    Slice<Post> findSliceWithMemberAndRestaurantBy(Pageable pageable, PostSearch postSearch);

    List<Hashtag> findAllHashtagByRestaurant(Restaurant restaurant);

}
