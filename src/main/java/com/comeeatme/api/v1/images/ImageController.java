package com.comeeatme.api.v1.images;

import com.comeeatme.api.common.dto.ApiResult;
import com.comeeatme.domain.images.service.ImageService;
import com.comeeatme.error.exception.InvalidImageException;
import com.comeeatme.security.annotation.CurrentUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestMapping("/v1/images")
@RestController
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/scaled")
    public ResponseEntity<ApiResult<List<Long>>> postScaled(
            @RequestPart List<MultipartFile> images, @CurrentUsername String username) {
        validateMultipartFileImages(images);
        List<Resource> resources = images.stream()
                .map(MultipartFile::getResource)
                .collect(Collectors.toList());
        List<Long> imageIds = imageService.saveImages(username, resources);
        ApiResult<List<Long>> result = ApiResult.success(imageIds);
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

}
