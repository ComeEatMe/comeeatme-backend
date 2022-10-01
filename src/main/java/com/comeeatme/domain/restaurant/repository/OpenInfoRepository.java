package com.comeeatme.domain.restaurant.repository;

import com.comeeatme.domain.restaurant.OpenInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpenInfoRepository extends JpaRepository<OpenInfo, Long> {
}
