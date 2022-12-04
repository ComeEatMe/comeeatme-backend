package com.comeeatme.domain.comment.repository;

import com.comeeatme.domain.comment.Comment;
import com.comeeatme.domain.post.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface CommentRepositoryCustom {

    boolean existsByIdAndUsernameAndUseYnIsTrue(Long commentId, String username);

    Slice<Comment> findSliceByPostWithMemberAndImage(Pageable pageable, Post post);

}
