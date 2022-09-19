package com.comeeatme.domain.category.repository;

import com.comeeatme.domain.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
