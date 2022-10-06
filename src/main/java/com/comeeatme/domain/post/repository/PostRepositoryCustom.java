package com.comeeatme.domain.post.repository;

public interface PostRepositoryCustom {

    boolean existsByIdAndUsernameAndUseYnIsTrue(Long postId, String username);
}
