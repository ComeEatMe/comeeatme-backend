package com.comeeatme.domain.image.repository;

import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findAllByMemberAndUseYnIsTrue(Member member);
}
