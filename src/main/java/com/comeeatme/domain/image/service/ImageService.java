package com.comeeatme.domain.image.service;

import com.comeeatme.domain.common.response.CreateResults;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.image.repository.ImageRepository;
import com.comeeatme.domain.image.response.RestaurantImage;
import com.comeeatme.domain.image.store.ImageStore;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.PostImage;
import com.comeeatme.domain.post.repository.PostImageRepository;
import com.comeeatme.domain.post.response.RestaurantPostImage;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ImageService {

    private final ImageStore imageStore;

    private final ImageRepository imageRepository;

    private final MemberRepository memberRepository;

    private final RestaurantRepository restaurantRepository;

    private final PostImageRepository postImageRepository;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    @Transactional
    public CreateResults<Long> saveImages(List<Resource> images, Long memberId) {
        Member member = getMemberById(memberId);
        List<Image> storedImages = imageRepository.saveAll(images.stream()
                .map(image -> {
                    String originName = image.getFilename();
                    String storedName = createStoredName(originName);
                    String url = imageStore.store(image, storedName);
                    return Image.builder()
                            .member(member)
                            .originName(originName)
                            .storedName(storedName)
                            .url(url)
                            .build();
                }).collect(Collectors.toList())
        );
        List<Long> imageIds = storedImages.stream()
                .map(Image::getId)
                .collect(Collectors.toList());
        return new CreateResults<>(imageIds);
    }

    private String createStoredName(String originalName) {
        String uuid = UUID.randomUUID().toString();
        String ext = extractExt(originalName);
        String now = LocalDateTime.now().format(dateTimeFormatter);
        return now + "/" + uuid + "." + ext;
    }

    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

    public boolean validateImageIds(List<Long> imageIds, Long memberId) {
        if (imageIds.size() != imageIds.stream().distinct().count()) {
            return false;
        }
        List<Image> images = getImagesByIds(imageIds);
        if (imageIds.size() != images.size()) {
            return false;
        }
        Member member = getMemberById(memberId);
        return images.stream()
                .filter(image -> !Objects.equals(image.getMember().getId(), member.getId()))
                .findAny()
                .isEmpty();
    }

    public boolean isNotOwnedByMember(Long memberId, Long imageId) {
        Image image = getImageById(imageId);
        return !Objects.equals(image.getMember().getId(), memberId);
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .filter(Member::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Member.id=" + memberId));
    }

    private Image getImageById(Long imageId) {
        return imageRepository.findById(imageId)
                .filter(Image::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Image.id=" + imageId));
    }

    private List<Image> getImagesByIds(List<Long> imageIds) {
        return imageRepository.findAllById(imageIds)
                .stream()
                .filter(Image::getUseYn)
                .collect(Collectors.toList());
    }

    public Slice<RestaurantImage> getRestaurantImages(Long restaurantId, Pageable pageable) {
        Restaurant restaurant = getRestaurantById(restaurantId);
        return postImageRepository.findSliceWithImageByRestaurantAndUseYnIsTrue(restaurant, pageable)
                .map(postImage -> RestaurantImage.builder()
                        .restaurantId(restaurant.getId())
                        .postId(postImage.getPost().getId())
                        .imageUrl(postImage.getImage().getUrl())
                        .build()
                );
    }

    public Map<Long, List<String>> getRestaurantIdToImages(List<Long> restaurantIds, Integer perImageNum) {
        List<Restaurant> restaurants = getRestaurantsByIds(restaurantIds);
        List<RestaurantPostImage> restaurantPostImages = postImageRepository.findImagesByRestaurantsAndPostUseYnIsTrue(
                restaurants, perImageNum);
        List<Long> postImageIds = restaurantPostImages.stream()
                .map(RestaurantPostImage::getPostImageId)
                .collect(Collectors.toList());
        List<PostImage> postImages = postImageRepository.findAllWithPostAndImageByIdIn(postImageIds);
        return postImages.stream()
                .collect(Collectors.groupingBy(
                        postImage -> postImage.getPost().getRestaurant().getId(),
                        Collectors.mapping(postImage -> postImage.getImage().getUrl(), Collectors.toList()))
                );
    }

    @Transactional
    public void deleteAllOfMember(Long memberId) {
        Member member = getMemberById(memberId);
        List<Image> images = imageRepository.findAllByMemberAndUseYnIsTrue(member);
        images.forEach(Image::delete);
    }

    private Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .filter(Restaurant::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant.id=" + id));
    }

    private List<Restaurant> getRestaurantsByIds(List<Long> restaurantIds) {
        List<Restaurant> restaurants = restaurantRepository.findAllById(restaurantIds);
        if (restaurantIds.size() != restaurants.size()) {
            throw new EntityNotFoundException("존재하지 않는 음식점 포함. Restaurant.id=" + restaurantIds);
        }
        boolean containDeleted = restaurants.stream()
                .anyMatch(restaurant -> !restaurant.getUseYn());
        if (containDeleted) {
            throw new EntityNotFoundException("삭제된 음식점 포함. Restaurant.id=" + restaurantIds);
        }
        return restaurants;
    }

}
