package com.comeeatme.domain.favorite.repository;

import com.comeeatme.domain.favorite.FavoriteGroup;
import com.comeeatme.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteGroupRepository extends JpaRepository<FavoriteGroup, Long> {

    Optional<FavoriteGroup> findByMemberAndName(Member member, String name);

    List<FavoriteGroup> findAllByMember(Member member);

}
