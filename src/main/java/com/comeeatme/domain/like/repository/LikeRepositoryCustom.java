package com.comeeatme.domain.like.repository;

import com.comeeatme.domain.like.Like;
import com.comeeatme.domain.like.response.LikeCount;
import com.comeeatme.domain.post.Post;

import java.util.List;

public interface LikeRepositoryCustom {

    List<LikeCount> countsGroupByPosts(List<Post> posts);

    List<Like> findByMemberIdAndPostIds(Long memberId, List<Long> postIds);

}
