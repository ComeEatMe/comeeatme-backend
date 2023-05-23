package com.comeeatme.domain.like.repository;

import com.comeeatme.domain.post.Post;

public interface LikeRepositoryCustom {

    void deleteAllByPost(Post post);

}
