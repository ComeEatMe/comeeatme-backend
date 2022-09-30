package com.comeeatme.domain.images.repository;

import com.comeeatme.domain.images.Images;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImagesRepository extends JpaRepository<Images, Long> {
}
