package com.comeeatme.domain.comment.repository;

public interface CommentRepositoryCustom {

    boolean existsByIdAndUsernameAndUseYnIsTrue(Long commentId, String username);
}
