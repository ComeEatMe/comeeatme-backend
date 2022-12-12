package com.comeeatme.domain.post.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.image.repository.ImageRepository;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.PostImage;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private MemberRepository memberRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private EntityManagerFactory emf;

    @Test
    void findAllWithImagesByPostIn() {
        // given
        Post post1 = postRepository.save(Post.builder()
                .member(memberRepository.getReferenceById(1L))
                .restaurant(restaurantRepository.getReferenceById(2L))
                .content("test-content-1")
                .build());
        Image image1 = imageRepository.save(Image.builder()
                .member(memberRepository.getReferenceById(1L))
                .originName("origin-name-1")
                .storedName("stored-name-1")
                .url("image-url-1")
                .build());
        PostImage postImage1 = postImageRepository.save(PostImage.builder()
                .post(post1)
                .image(image1)
                .build());

        Post post2 = postRepository.save(Post.builder()
                .member(memberRepository.getReferenceById(1L))
                .restaurant(restaurantRepository.getReferenceById(2L))
                .content("test-content-2")
                .build());
        Image image2_1 = imageRepository.save(Image.builder()
                .member(memberRepository.getReferenceById(1L))
                .originName("origin-name-2-1")
                .storedName("stored-name-2-1")
                .url("image-url-2-1")
                .build());
        PostImage postImage2_1 = postImageRepository.save(PostImage.builder()
                .post(post2)
                .image(image2_1)
                .build());
        Image image2_2 = imageRepository.save(Image.builder()
                .member(memberRepository.getReferenceById(1L))
                .originName("origin-name-2-2")
                .storedName("stored-name-2-2")
                .url("image-url-2-2")
                .build());
        PostImage postImage2_2 = postImageRepository.save(PostImage.builder()
                .post(post2)
                .image(image2_2)
                .build());

        Post post3 = postRepository.save(Post.builder()
                .member(memberRepository.getReferenceById(1L))
                .restaurant(restaurantRepository.getReferenceById(2L))
                .content("test-content-3")
                .build());
        Image image3 = imageRepository.save(Image.builder()
                .member(memberRepository.getReferenceById(1L))
                .originName("origin-name-3")
                .storedName("stored-name-3")
                .url("image-url-3")
                .build());
        PostImage postImage3 = postImageRepository.save(PostImage.builder()
                .post(post3)
                .image(image3)
                .build());

        em.flush();
        em.clear();

        // when
        List<PostImage> postImages = postImageRepository.findAllWithImageByPostIn(List.of(post1, post2));

        // then
        PersistenceUnitUtil persistenceUnitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
        assertThat(postImages).hasSize(3);
        assertThat(postImages).extracting("id")
                .containsOnly(postImage1.getId(), postImage2_1.getId(), postImage2_2.getId());
        for (PostImage postImage : postImages) {
            assertThat(persistenceUnitUtil.isLoaded(postImage.getImage())).isTrue();
        }
    }

    @Test
    void findAllWithImageByPost() {
        // given
        Image image1 = imageRepository.save(Image.builder()
                .member(memberRepository.getReferenceById(1L))
                .originName("origin-name-1")
                .storedName("stored-name-1")
                .url("image-url-1")
                .build());
        PostImage postImage1 = postImageRepository.save(PostImage.builder()
                .post(postRepository.getReferenceById(2L))
                .image(image1)
                .build());

        Image image2 = imageRepository.save(Image.builder()
                .member(memberRepository.getReferenceById(1L))
                .originName("origin-name-1")
                .storedName("stored-name-1")
                .url("image-url-1")
                .build());
        PostImage postImage2 = postImageRepository.save(PostImage.builder()
                .post(postRepository.getReferenceById(3L))
                .image(image2)
                .build());

        em.flush();
        em.clear();

        // when
        List<PostImage> result = postImageRepository.findAllWithImageByPost(postRepository.getReferenceById(2L));

        // then
        PersistenceUnitUtil unitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
        assertThat(result)
                .hasSize(1)
                .extracting("id").containsExactly(postImage1.getId());
        result.stream()
                .map(PostImage::getImage)
                .forEach(image -> assertThat(unitUtil.isLoaded(image)).isTrue());
    }

    @Test
    void findAllWithPostAndImageById() {
        // given
        Post post = postRepository.save(
                Post.builder()
                        .member(memberRepository.getReferenceById(10L))
                        .restaurant(restaurantRepository.getReferenceById(20L))
                        .content("content")
                        .build()
        );

        List<PostImage> postImages = IntStream.range(0, 2)
                .mapToObj(i -> {
                    Image image = imageRepository.save(
                            Image.builder()
                                    .member(memberRepository.getReferenceById(10L))
                                    .originName("origin-name" + i)
                                    .storedName("stored-name" + i)
                                    .url("url" + i)
                                    .build()
                    );
                    return postImageRepository.save(
                            PostImage.builder()
                                    .post(post)
                                    .image(image)
                                    .build()
                    );
                })
                .collect(Collectors.toList());

        // when
        List<Long> postImageIds = postImages.stream()
                .map(PostImage::getId)
                .collect(Collectors.toList());
        List<PostImage> result = postImageRepository.findAllWithPostAndImageByIdIn(postImageIds);

        // then
        assertThat(result)
                .hasSize(2)
                .extracting("id").containsOnly(postImageIds.toArray());
        for (PostImage postImage : result) {
            PersistenceUnitUtil persistenceUnitUtil = emf.getPersistenceUnitUtil();
            assertThat(persistenceUnitUtil.isLoaded(postImage.getImage())).isTrue();
            assertThat(persistenceUnitUtil.isLoaded(postImage.getPost())).isTrue();
        }
    }

}