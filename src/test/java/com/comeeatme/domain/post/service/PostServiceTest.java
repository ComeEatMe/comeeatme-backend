package com.comeeatme.domain.post.service;

import com.comeeatme.domain.images.Images;
import com.comeeatme.domain.images.repository.ImagesRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.HashTag;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.PostImage;
import com.comeeatme.domain.post.repository.PostImageRepository;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.post.request.PostCreate;
import com.comeeatme.domain.post.request.PostEdit;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private ImagesRepository imagesRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MemberRepository memberRepository;

    @Test
    void create() {
        // given
        PostCreate postCreate = PostCreate.builder()
                .restaurantId(1L)
                .hashTags(Set.of(HashTag.STRONG_TASTE, HashTag.CLEANLINESS))
                .imageIds(List.of(2L, 3L, 4L))
                .content("test-content")
                .build();

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findByUsername(anyString()))
                .willReturn(Optional.of(member));

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findById(postCreate.getRestaurantId()))
                .willReturn(Optional.of(restaurant));

        Post post = mock(Post.class);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        given(postRepository.save(postCaptor.capture())).willReturn(post);

        Images image1 = mock(Images.class);
        Images image2 = mock(Images.class);
        Images image3 = mock(Images.class);
        given(image1.getUseYn()).willReturn(true);
        given(image2.getUseYn()).willReturn(true);
        given(image3.getUseYn()).willReturn(true);
        given(imagesRepository.findAllById(postCreate.getImageIds()))
                .willReturn(List.of(image1, image2, image3));

        given(post.getId()).willReturn(5L);

        // when
        long postId = postService.create(postCreate, "test-username");

        // then
        Post capturedPost = postCaptor.getValue();
        assertThat(capturedPost.getMember()).isEqualTo(member);
        assertThat(capturedPost.getRestaurant()).isEqualTo(restaurant);
        assertThat(capturedPost.getHashTags()).isEqualTo(postCreate.getHashTags());
        assertThat(capturedPost.getContent()).isEqualTo(postCreate.getContent());

        ArgumentCaptor<List<PostImage>> postImagesCaptor = ArgumentCaptor.forClass(List.class);
        then(postImageRepository).should().saveAll(postImagesCaptor.capture());
        List<PostImage> capturedPostImages = postImagesCaptor.getValue();
        assertThat(capturedPostImages).hasSize(3);
        assertThat(capturedPostImages).extracting("post")
                .containsExactly(post, post, post);
        assertThat(capturedPostImages).extracting("image")
                .containsExactly(image1, image2, image3);

        assertThat(postId).isEqualTo(5L);
    }

    @Test
    void edit() {
        // given
        Restaurant postRestaurant = mock(Restaurant.class);
        given(postRestaurant.getId()).willReturn(2L);

        Post post = Post.builder()
                .id(1L)
                .restaurant(postRestaurant)
                .hashTags(Set.of(HashTag.STRONG_TASTE, HashTag.DATE))
                .content("post-content")
                .build();

        PostEdit postEdit = PostEdit.builder()
                .restaurantId(3L)
                .hashTags(Set.of(HashTag.STRONG_TASTE, HashTag.CLEANLINESS))
                .content("edited-content")
                .build();

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Restaurant editedRestaurant = mock(Restaurant.class);
        given(editedRestaurant.getId()).willReturn(3L);
        given(editedRestaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findById(3L)).willReturn(Optional.of(editedRestaurant));

        // when
        Long editedPostId = postService.edit(postEdit, 1L);

        // then
        assertThat(post.getRestaurant().getId()).isEqualTo(3L);
        assertThat(post.getHashTags()).containsOnly(HashTag.STRONG_TASTE, HashTag.CLEANLINESS);
        assertThat(post.getContent()).isEqualTo("edited-content");
        assertThat(editedPostId).isEqualTo(1L);
    }

    @Test
    void edit_EqualsRestaurantId() {
        // given
        Restaurant postRestaurant = mock(Restaurant.class);
        given(postRestaurant.getId()).willReturn(2L);

        Post post = Post.builder()
                .id(1L)
                .restaurant(postRestaurant)
                .hashTags(Set.of(HashTag.STRONG_TASTE, HashTag.DATE))
                .content("post-content")
                .build();

        PostEdit postEdit = PostEdit.builder()
                .restaurantId(2L)
                .hashTags(Set.of(HashTag.STRONG_TASTE, HashTag.CLEANLINESS))
                .content("edited-content")
                .build();

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        Long editedPostId = postService.edit(postEdit, 1L);

        // then
        assertThat(post.getRestaurant().getId()).isEqualTo(2L);
        assertThat(post.getHashTags()).containsOnly(HashTag.STRONG_TASTE, HashTag.CLEANLINESS);
        assertThat(post.getContent()).isEqualTo("edited-content");
        assertThat(editedPostId).isEqualTo(1L);
        then(restaurantRepository).should(never()).findById(any());
    }

    @Test
    void isNotOwnedByMember_False() {
        // given
        given(postRepository.existsByIdAndUsernameAndUseYnIsTrue(1L, "test-username")).willReturn(true);

        // then
        assertThat(postService.isNotOwnedByMember(1L, "test-username")).isFalse();
    }

    @Test
    void isNotOwnedByMember_True() {
        // given
        given(postRepository.existsByIdAndUsernameAndUseYnIsTrue(1L, "test-username")).willReturn(false);

        // then
        assertThat(postService.isNotOwnedByMember(1L, "test-username")).isTrue();
    }

}