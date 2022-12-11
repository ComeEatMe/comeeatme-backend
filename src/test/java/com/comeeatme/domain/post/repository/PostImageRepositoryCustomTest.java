package com.comeeatme.domain.post.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.image.repository.ImageRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.PostImage;
import com.comeeatme.domain.post.response.RestaurantPostImage;
import com.comeeatme.domain.restaurant.Restaurant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class PostImageRepositoryCustomTest {

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private EntityManagerFactory emf;

    @Test
    void findSliceWithImageByRestaurant() {
        // given
        Post post = postRepository.save(Post.builder()
                .restaurant(Restaurant.builder().id(1L).build())
                .member(Member.builder().id(2L).build())
                .content("content")
                .build());
        Image image = imageRepository.save(Image.builder()
                .storedName("storedName")
                .originName("originName")
                .url("url")
                .member(Member.builder().id(2L).build())
                .build());
        PostImage postImage = postImageRepository.save(PostImage.builder()
                .post(post)
                .image(image)
                .build());

        // when
        Restaurant restaurant = Restaurant.builder().id(1L).build();
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<PostImage> result = postImageRepository.findSliceWithImageByRestaurantAndUseYnIsTrue(restaurant, pageRequest);

        // then
        List<PostImage> content = result.getContent();
        assertThat(content).hasSize(1);

        PostImage foundPostImage = content.get(0);
        assertThat(foundPostImage.getId()).isEqualTo(postImage.getId());
        assertThat(emf.getPersistenceUnitUtil().isLoaded(foundPostImage.getImage())).isTrue();
    }

    @Test
    void findSliceWithImageByRestaurant_Deleted() {
        // given
        Post post = postRepository.save(Post.builder()
                .restaurant(Restaurant.builder().id(1L).build())
                .member(Member.builder().id(2L).build())
                .content("content")
                .build());
        Image image = imageRepository.save(Image.builder()
                .storedName("storedName")
                .originName("originName")
                .url("url")
                .member(Member.builder().id(2L).build())
                .build());
        PostImage postImage = postImageRepository.save(PostImage.builder()
                .post(post)
                .image(image)
                .build());
        image.delete();

        // when
        Restaurant restaurant = Restaurant.builder().id(1L).build();
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<PostImage> result = postImageRepository.findSliceWithImageByRestaurantAndUseYnIsTrue(restaurant, pageRequest);

        // then
        List<PostImage> content = result.getContent();
        assertThat(content).isEmpty();
    }

    @Test
    void findSliceWithImageByRestaurant_RestaurantNotEqual() {
        // given
        Post post = postRepository.save(Post.builder()
                .restaurant(Restaurant.builder().id(1L).build())
                .member(Member.builder().id(2L).build())
                .content("content")
                .build());
        Image image = imageRepository.save(Image.builder()
                .storedName("storedName")
                .originName("originName")
                .url("url")
                .member(Member.builder().id(2L).build())
                .build());
        PostImage postImage = postImageRepository.save(PostImage.builder()
                .post(post)
                .image(image)
                .build());

        // when
        Restaurant restaurant = Restaurant.builder().id(2L).build();
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<PostImage> result = postImageRepository.findSliceWithImageByRestaurantAndUseYnIsTrue(restaurant, pageRequest);

        // then
        List<PostImage> content = result.getContent();
        assertThat(content).isEmpty();
    }

    @Test
    void findImagesByRestaurantsAndPostUseYnIsTrue() {
        // given
        List<Post> posts = postRepository.saveAll(List.of(
                Post.builder()
                        .member(Member.builder().id(10L).build())
                        .restaurant(Restaurant.builder().id(20L).build())
                        .content("content-1")
                        .build(),
                Post.builder()
                        .member(Member.builder().id(10L).build())
                        .restaurant(Restaurant.builder().id(21L).build())
                        .content("content-2")
                        .build(),
                Post.builder()
                        .member(Member.builder().id(10L).build())
                        .restaurant(Restaurant.builder().id(21L).build())
                        .content("content-3")
                        .build()
        ));

        List<PostImage> postImages = postImageRepository.saveAll(List.of(
                PostImage.builder()
                        .image(imageRepository.getReferenceById(30L))
                        .post(posts.get(0))
                        .build(),
                PostImage.builder()
                        .image(imageRepository.getReferenceById(31L))
                        .post(posts.get(0))
                        .build(),
                PostImage.builder()
                        .image(imageRepository.getReferenceById(32L))
                        .post(posts.get(1))
                        .build(),
                PostImage.builder()
                        .image(imageRepository.getReferenceById(33L))
                        .post(posts.get(1))
                        .build(),
                PostImage.builder()
                        .image(imageRepository.getReferenceById(34L))
                        .post(posts.get(1))
                        .build()
        ));


        // when
        List<RestaurantPostImage> result = postImageRepository.findImagesByRestaurantsAndPostUseYnIsTrue(
                List.of(Restaurant.builder().id(20L).build(), Restaurant.builder().id(21L).build()), 2);

        // then
        Map<Long, List<Long>> restaurantIdToImageIds = result.stream()
                .collect(Collectors.groupingBy(
                        RestaurantPostImage::getRestaurantId,
                        Collectors.mapping(RestaurantPostImage::getPostImageId, Collectors.toList())
                ));
        assertThat(restaurantIdToImageIds).containsOnlyKeys(20L, 21L);
        assertThat(restaurantIdToImageIds.get(20L)).containsOnly(
                postImages.get(0).getId(), postImages.get(1).getId());
        assertThat(restaurantIdToImageIds.get(21L)).containsOnly(
                postImages.get(2).getId(), postImages.get(3).getId());
    }

}