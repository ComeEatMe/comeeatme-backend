package com.comeeatme.domain.post.repository;

import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.request.PostSearch;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface PostRepositoryCustom {

    Slice<Post> findSliceWithMemberAndRestaurantBy(Pageable pageable, PostSearch postSearch);

}
