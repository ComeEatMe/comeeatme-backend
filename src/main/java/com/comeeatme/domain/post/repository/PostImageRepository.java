package com.comeeatme.domain.post.repository;

import com.comeeatme.domain.post.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
}
