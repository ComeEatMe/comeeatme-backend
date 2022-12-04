package com.comeeatme.domain.like.repository;

import com.comeeatme.domain.like.Like;
import com.comeeatme.domain.post.Post;

import java.util.List;

public interface LikeRepositoryCustom {

    List<Like> findByMemberIdAndPostIds(Long memberId, List<Long> postIds);

    void deleteAllByPost(Post post);

}
