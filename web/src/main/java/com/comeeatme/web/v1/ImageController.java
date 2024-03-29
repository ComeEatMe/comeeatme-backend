package com.comeeatme.web.v1;

import com.comeeatme.api.account.AccountService;
import com.comeeatme.api.common.response.CreateResults;
import com.comeeatme.api.exception.InvalidImageException;
import com.comeeatme.api.image.ImageService;
import com.comeeatme.domain.image.response.RestaurantImage;
import com.comeeatme.web.common.response.ApiResult;
import com.comeeatme.web.security.annotation.LoginUsername;
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

    private final AccountService accountService;

    @PostMapping("/images/scaled")
    public ResponseEntity<ApiResult<CreateResults<Long>>> postScaled(
            @RequestPart List<MultipartFile> images, @LoginUsername String username) {
        Long memberId = accountService.getMemberId(username);
        validateMultipartFileImages(images);
        List<Resource> resources = images.stream()
                .map(MultipartFile::getResource)
                .collect(Collectors.toList());
        CreateResults<Long> createResults = imageService.saveImages(resources, memberId);
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
