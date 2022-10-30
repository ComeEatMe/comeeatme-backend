package com.comeeatme.api.v1;

import com.comeeatme.api.common.response.ApiResult;
import com.comeeatme.domain.common.response.CreateResults;
import com.comeeatme.domain.image.response.RestaurantImage;
import com.comeeatme.domain.image.service.ImageService;
import com.comeeatme.error.exception.InvalidImageException;
import com.comeeatme.security.annotation.CurrentUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/images/scaled")
    public ResponseEntity<ApiResult<CreateResults<Long>>> postScaled(
            @RequestPart List<MultipartFile> images, @CurrentUsername String username) {
        validateMultipartFileImages(images);
        List<Resource> resources = images.stream()
                .map(MultipartFile::getResource)
                .collect(Collectors.toList());
        CreateResults<Long> createResults = imageService.saveImages(username, resources);
        ApiResult<CreateResults<Long>> result = ApiResult.success(createResults);
        return ResponseEntity.ok(result);
    }

    private void validateMultipartFileImages(List<MultipartFile> images) {
        images.stream()
                .filter(image -> !validateMultipartFileImage(image))
                .findAny()
                .ifPresent(image -> {
                    throw new InvalidImageException(String.format(
                            "isEmpty=%s, contentType=%s", image.isEmpty(), image.getContentType()));
                });
    }

    private boolean validateMultipartFileImage(MultipartFile image) {
        return Optional.of(image)
                .filter(multipartFile -> !multipartFile.isEmpty())
                .filter(multipartFile -> Optional.ofNullable(multipartFile.getContentType())
                        .filter(contentType -> contentType.startsWith("image/"))
                        .isPresent()
                )
                .isPresent();
    }

    @GetMapping("/restaurants/{restaurantId}/images")
    public ResponseEntity<ApiResult<Slice<RestaurantImage>>> getRestaurantImages(
            Pageable pageable, @PathVariable Long restaurantId) {
        Slice<RestaurantImage> restaurantImages = imageService.getRestaurantImages(restaurantId, pageable);
        ApiResult<Slice<RestaurantImage>> apiResult = ApiResult.success(restaurantImages);
        return ResponseEntity.ok(apiResult);
    }

}
