package com.comeeatme.domain.post.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.image.repository.ImageRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.PostImage;
import com.comeeatme.domain.restaurant.Restaurant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class PostImageRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private EntityManager em;

    @Test
    void findAllWithImagesByPostIn() {
        // given
        Post post1 = postRepository.save(Post.builder()
                .member(Member.builder().id(1L).build())
                .restaurant(Restaurant.builder().id(2L).build())
                .content("test-content-1")
                .build());
        Image image1 = imageRepository.save(Image.builder()
                .member(Member.builder().id(1L).build())
                .originName("origin-name-1")
                .storedName("stored-name-1")
                .url("image-url-1")
                .build());
        PostImage postImage1 = postImageRepository.save(PostImage.builder()
                .post(post1)
                .image(image1)
                .build());

        Post post2 = postRepository.save(Post.builder()
                .member(Member.builder().id(1L).build())
                .restaurant(Restaurant.builder().id(2L).build())
                .content("test-content-2")
                .build());
        Image image2 = imageRepository.save(Image.builder()
                .member(Member.builder().id(1L).build())
                .originName("origin-name-2")
                .storedName("stored-name-2")
                .url("image-url-2")
                .build());
        PostImage postImage2 = postImageRepository.save(PostImage.builder()
                .post(post2)
                .image(image2)
                .build());

        Post post3 = postRepository.save(Post.builder()
                .member(Member.builder().id(1L).build())
                .restaurant(Restaurant.builder().id(2L).build())
                .content("test-content-2")
                .build());
        Image image3 = imageRepository.save(Image.builder()
                .member(Member.builder().id(1L).build())
                .originName("origin-name-2")
                .storedName("stored-name-2")
                .url("image-url-2")
                .build());
        PostImage postImage3 = postImageRepository.save(PostImage.builder()
                .post(post3)
                .image(image3)
                .build());
        postImage3.delete();

        em.flush();
        em.clear();

        // when
        List<PostImage> postImages = postImageRepository.findAllWithImagesByPostInAndUseYnIsTrue(List.of(post1, post2));

        // then
        assertThat(postImages).hasSize(2);
        assertThat(postImages).extracting("id").containsExactly(postImage1.getId(), postImage2.getId());
        assertThat(postImages).extracting("post").extracting("id")
                .containsExactly(post1.getId(), post2.getId());
        assertThat(postImages).extracting("image").extracting("id")
                .containsExactly(image1.getId(), image2.getId());
        assertThat(postImages).extracting("image").extracting("url")
                .containsExactly(image1.getUrl(), image2.getUrl());
    }
}