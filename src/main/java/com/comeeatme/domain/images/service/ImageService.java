package com.comeeatme.domain.images.service;

import com.comeeatme.domain.images.Images;
import com.comeeatme.domain.images.repository.ImagesRepository;
import com.comeeatme.domain.images.store.ImageStore;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ImageService {

    private final ImageStore imageStore;

    private final ImagesRepository imagesRepository;

    private final MemberRepository memberRepository;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Transactional
    public List<Long> saveImages(String username, List<Resource> images) {
        Member member = getMemberByUsername(username);
        List<Images> storedImages = imagesRepository.saveAll(images.stream()
                .map(image -> {
                    String originName = image.getFilename();
                    String storedName = createStoredName(originName);
                    String url = imageStore.store(image, storedName);
                    return Images.builder()
                            .member(member)
                            .originName(originName)
                            .storedName(storedName)
                            .url(url)
                            .build();
                }).collect(Collectors.toList())
        );
        return storedImages.stream()
                .map(Images::getId)
                .collect(Collectors.toList());
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

    public boolean validateImageIds(List<Long> imageIds, String username) {
        if (imageIds.size() != imageIds.stream().distinct().count()) {
            return false;
        }
        List<Images> images = getImagesByIds(imageIds);
        if (imageIds.size() != images.size()) {
            return false;
        }
        Member member = getMemberByUsername(username);
        return images.stream()
                .filter(image -> !Objects.equals(image.getMember().getId(), member.getId()))
                .findAny()
                .isEmpty();
    }

    private Member getMemberByUsername(String username) {
        return memberRepository.findByUsername(username)
                .filter(Member::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Member username=" + username));
    }

    private List<Images> getImagesByIds(List<Long> imageIds) {
        return imagesRepository.findAllById(imageIds)
                .stream()
                .filter(Images::getUseYn)
                .collect(Collectors.toList());
    }
}
