package com.comeeatme.domain.post.repository;

import com.comeeatme.domain.post.PostHashtag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostHashtagRepository extends JpaRepository<PostHashtag, Long>, PostHashtagRepositoryCustom {
}
