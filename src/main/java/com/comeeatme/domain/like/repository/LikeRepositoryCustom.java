package com.comeeatme.domain.like.repository;

import com.comeeatme.domain.like.response.LikeCount;
import com.comeeatme.domain.like.response.LikedResult;
import com.comeeatme.domain.post.Post;

import java.util.List;

public interface LikeRepositoryCustom {

    List<LikeCount> countsGroupByPosts(List<Post> posts);

    List<LikedResult> existsByPostIdsAndUsername(List<Long> postIds, String username);

    List<LikedResult> existsByPostIdsAndMemberId(List<Long> postIds, Long memberId);
}
