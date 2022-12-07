package com.comeeatme.domain.image.service;

import com.comeeatme.domain.common.response.CreateResults;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.image.repository.ImageRepository;
import com.comeeatme.domain.image.response.RestaurantImage;
import com.comeeatme.domain.image.store.ImageStore;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.PostImage;
import com.comeeatme.domain.post.repository.PostImageRepository;
import com.comeeatme.domain.post.response.RestaurantPostImage;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;


@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @InjectMocks
    private ImageService imageService;

    @Mock
    private ImageStore imageStore;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private PostImageRepository postImageRepository;


    @Test
    void saveImages() {
        // given
        Member mockMember = mock(Member.class);
        given(mockMember.getUseYn()).willReturn(true);
        given(memberRepository.findById(10L)).willReturn(Optional.of(mockMember));

        Resource mockResource = mock(Resource.class);
        List<Resource> resources = List.of(mockResource);
        given(mockResource.getFilename()).willReturn("test-filename.jpg");

        ArgumentCaptor<String> storedNameCaptor = ArgumentCaptor.forClass(String.class);
        given(imageStore.store(eq(mockResource), storedNameCaptor.capture())).willReturn("test-image-url");

        Image mockImage = mock(Image.class);
        given(mockImage.getId()).willReturn(1L);
        ArgumentCaptor<List<Image>> imagesCaptor = ArgumentCaptor.forClass(List.class);
        given(imageRepository.saveAll(imagesCaptor.capture())).willReturn(List.of(mockImage));

        // when
        CreateResults<Long> result = imageService.saveImages(resources, 10L);

        // then
        String storedName = storedNameCaptor.getValue();
        int extPos = storedName.lastIndexOf(".");
        assertThat(storedName.substring(extPos + 1)).isEqualTo("jpg");

        List<Image> images = imagesCaptor.getValue();
        assertThat(images).hasSize(1);
        assertThat(images).extracting("storedName").containsExactly(storedName);
        assertThat(images).extracting("originName").containsExactly("test-filename.jpg");
        assertThat(images).extracting("url").containsExactly("test-image-url");

        assertThat(result.getIds())
                .hasSize(1)
                .containsExactly(1L);
    }

    @Test
    void validateImageIds() {
        // given
        List<Long> imageIds = List.of(1L, 2L, 3L);
        Image image1 = mock(Image.class);
        Image image2 = mock(Image.class);
        Image image3 = mock(Image.class);

        given(image1.getUseYn()).willReturn(true);
        given(image2.getUseYn()).willReturn(true);
        given(image3.getUseYn()).willReturn(true);

        given(imageRepository.findAllById(imageIds)).willReturn(List.of(image1, image2, image3));

        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        given(member.getUseYn()).willReturn(true);

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        given(image1.getMember()).willReturn(member);
        given(image2.getMember()).willReturn(member);
        given(image3.getMember()).willReturn(member);

        // expected
        assertThat(imageService.validateImageIds(imageIds, 1L)).isTrue();
    }

    @Test
    void validateImageIds_DuplicatedImageIds() {
        // given
        List<Long> imageIds = List.of(1L, 2L, 2L);

        // expected
        assertThat(imageService.validateImageIds(imageIds, 1L)).isFalse();
    }

    @Test
    void validateImageIds_DeletedImageId() {
        // given
        List<Long> imageIds = List.of(1L, 2L, 3L);
        Image image1 = mock(Image.class);
        Image image2 = mock(Image.class);
        Image image3 = mock(Image.class);

        given(image1.getUseYn()).willReturn(true);
        given(image2.getUseYn()).willReturn(true);
        given(image3.getUseYn()).willReturn(false);

        given(imageRepository.findAllById(imageIds)).willReturn(List.of(image1, image2, image3));

        // expected
        assertThat(imageService.validateImageIds(imageIds, 1L)).isFalse();
    }

    @Test
    void validateImageIds_NotOwnedByMember() {
        // given
        List<Long> imageIds = List.of(1L, 2L, 3L);
        Image image1 = mock(Image.class);
        Image image2 = mock(Image.class);
        Image image3 = mock(Image.class);

        given(image1.getUseYn()).willReturn(true);
        given(image2.getUseYn()).willReturn(true);
        given(image3.getUseYn()).willReturn(true);

        given(imageRepository.findAllById(imageIds)).willReturn(List.of(image1, image2, image3));

        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        given(member.getUseYn()).willReturn(true);

        Member otherMember = mock(Member.class);
        given(otherMember.getId()).willReturn(2L);

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        given(image1.getMember()).willReturn(member);
        given(image2.getMember()).willReturn(member);
        given(image3.getMember()).willReturn(otherMember);

        // expected
        assertThat(imageService.validateImageIds(imageIds, 1L)).isFalse();
    }

    @Test
    void isNotOwnedNyMember_True() {
        // given
        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);

        Image image = mock(Image.class);
        given(image.getUseYn()).willReturn(true);
        given(image.getMember()).willReturn(member);
        given(imageRepository.findById(2L)).willReturn(Optional.of(image));

        // when
        boolean result = imageService.isNotOwnedByMember(3L, 2L);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void isNotOwnedNyMember_False() {
        // given
        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);

        Image image = mock(Image.class);
        given(image.getUseYn()).willReturn(true);
        given(image.getMember()).willReturn(member);
        given(imageRepository.findById(2L)).willReturn(Optional.of(image));

        // when
        boolean result = imageService.isNotOwnedByMember(1L, 2L);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void getRestaurantImages() {
        // given
        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getUseYn()).willReturn(true);
        given(restaurant.getId()).willReturn(1L);
        given(restaurantRepository.findById(1L)).willReturn(Optional.of(restaurant));

        Post post = mock(Post.class);
        given(post.getId()).willReturn(2L);
        Image image = mock(Image.class);
        given(image.getUrl()).willReturn("image-url");
        PostImage postImage = mock(PostImage.class);
        given(postImage.getPost()).willReturn(post);
        given(postImage.getImage()).willReturn(image);

        PageRequest pageRequest = PageRequest.of(0, 10);
        given(postImageRepository.findSliceWithImageByRestaurantAndUseYnIsTrue(restaurant, pageRequest))
                .willReturn(new SliceImpl<>(List.of(postImage)));

        // when
        Slice<RestaurantImage> result = imageService.getRestaurantImages(1L, pageRequest);

        // then
        RestaurantImage restaurantImage = result.getContent().get(0);
        assertThat(restaurantImage.getRestaurantId()).isEqualTo(1L);
        assertThat(restaurantImage.getPostId()).isEqualTo(2L);
        assertThat(restaurantImage.getImageUrl()).isEqualTo("image-url");
    }

    @Test
    void getRestaurantIdToImages() {
        // given
        Restaurant restaurant1 = mock(Restaurant.class);
        given(restaurant1.getUseYn()).willReturn(true);
        given(restaurant1.getId()).willReturn(1L);
        Restaurant restaurant2 = mock(Restaurant.class);
        given(restaurant2.getUseYn()).willReturn(true);
        given(restaurant2.getId()).willReturn(2L);
        given(restaurantRepository.findAllById(List.of(1L, 2L)))
                .willReturn(List.of(restaurant1, restaurant2));

        given(postImageRepository.findImagesByRestaurantsAndPostUseYnIsTrue(
                List.of(restaurant1, restaurant2), 2)).willReturn(
                        List.of(
                                new RestaurantPostImage(1L, 10L),
                                new RestaurantPostImage(1L, 11L),
                                new RestaurantPostImage(2L, 12L)
                        )
        );

        Post post1 = mock(Post.class);
        given(post1.getRestaurant()).willReturn(restaurant1);

        Image image1 = mock(Image.class);
        given(image1.getUrl()).willReturn("url-1");
        PostImage postImage1 = mock(PostImage.class);
        given(postImage1.getPost()).willReturn(post1);
        given(postImage1.getImage()).willReturn(image1);

        Image image2 = mock(Image.class);
        given(image2.getUrl()).willReturn("url-2");
        PostImage postImage2 = mock(PostImage.class);
        given(postImage2.getPost()).willReturn(post1);
        given(postImage2.getImage()).willReturn(image2);

        Post post2 = mock(Post.class);
        given(post2.getRestaurant()).willReturn(restaurant2);

        Image image3 = mock(Image.class);
        given(image3.getUrl()).willReturn("url-3");
        PostImage postImage3 = mock(PostImage.class);
        given(postImage3.getPost()).willReturn(post2);
        given(postImage3.getImage()).willReturn(image3);

        given(postImageRepository.findAllWithPostAndImageByIdIn(List.of(10L, 11L, 12L)))
                .willReturn(List.of(postImage1, postImage2, postImage3));

        // when
        Map<Long, List<String>> result = imageService.getRestaurantIdToImages(List.of(1L, 2L), 2);

        // then
        assertThat(result).containsOnlyKeys(1L, 2L);
        assertThat(result.get(1L))
                .hasSize(2)
                .containsOnly("url-1", "url-2");
        assertThat(result.get(2L))
                .hasSize(1)
                .containsOnly("url-3");
    }

}