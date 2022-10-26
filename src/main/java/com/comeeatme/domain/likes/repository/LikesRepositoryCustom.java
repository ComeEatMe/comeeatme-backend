package com.comeeatme.domain.likes.repository;

import com.comeeatme.domain.likes.response.LikeCount;
import com.comeeatme.domain.likes.response.LikedResult;
import com.comeeatme.domain.post.Post;

import java.util.List;

public interface LikesRepositoryCustom {

    List<LikeCount> countsGroupByPosts(List<Post> posts);

    List<LikedResult> existsByPostIdsAndUsername(List<Long> postIds, String username);

    List<LikedResult> existsByPostIdsAndMemberId(List<Long> postIds, Long memberId);
}
