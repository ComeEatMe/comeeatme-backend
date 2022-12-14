package com.comeeatme.domain.comment.repository;

import com.comeeatme.domain.comment.Comment;
import com.comeeatme.domain.post.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface CommentRepositoryCustom {

    Slice<Comment> findSliceByPostWithMemberAndImage(Pageable pageable, Post post);

    void updateUseYnFalseByPostIn(List<Post> posts);

}
