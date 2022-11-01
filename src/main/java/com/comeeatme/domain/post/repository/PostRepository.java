package com.comeeatme.domain.post.repository;

import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.restaurant.Restaurant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    @EntityGraph(attributePaths = "restaurant")
    Slice<Post> findSliceWithRestaurantByMemberAndUseYnIsTrue(Pageable pageable, Member member);

    @EntityGraph(attributePaths = {"member", "member.image"})
    Slice<Post> findSliceWithMemberByRestaurantAndUseYnIsTrue(Pageable pageable, Restaurant restaurant);

}
