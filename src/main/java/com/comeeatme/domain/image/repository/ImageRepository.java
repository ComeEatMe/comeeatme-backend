package com.comeeatme.domain.image.repository;

import com.comeeatme.domain.image.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
