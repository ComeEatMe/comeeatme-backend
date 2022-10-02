package com.comeeatme.domain.post.repository;

import com.comeeatme.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
