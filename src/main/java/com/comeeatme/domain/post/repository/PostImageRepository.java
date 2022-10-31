package com.comeeatme.domain.post.repository;

import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.PostImage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long>, PostImageRepositoryCustom {

    @EntityGraph(attributePaths = "image")
    List<PostImage> findAllWithImageByPostIn(List<Post> posts);

    @EntityGraph(attributePaths = "image")
    List<PostImage> findAllWithImageByPost(Post post);

}
