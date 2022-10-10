package com.comeeatme.domain.post.repository;

import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.request.PostSearch;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface PostRepositoryCustom {

    boolean existsByIdAndUsernameAndUseYnIsTrue(Long postId, String username);

    Slice<Post> findAllWithMemberAndRestaurant(Pageable pageable, PostSearch postSearch);
}
