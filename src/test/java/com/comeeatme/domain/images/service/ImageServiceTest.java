package com.comeeatme.domain.images.service;

import com.comeeatme.domain.images.Images;
import com.comeeatme.domain.images.repository.ImagesRepository;
import com.comeeatme.domain.images.store.ImageStore;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;


@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @InjectMocks
    private ImageService imageService;

    @Mock
    private ImageStore mockImageStore;

    @Mock
    private ImagesRepository mockImagesRepository;

    @Mock
    private MemberRepository mockMemberRepository;

    @Test
    void saveImages() {
        // given
        Member mockMember = mock(Member.class);
        given(mockMember.getUseYn()).willReturn(true);
        given(mockMemberRepository.findByUsername(anyString())).willReturn(Optional.of(mockMember));

        Resource mockResource = mock(Resource.class);
        List<Resource> resources = List.of(mockResource);
        given(mockResource.getFilename()).willReturn("test-filename.jpg");

        ArgumentCaptor<String> storedNameCaptor = ArgumentCaptor.forClass(String.class);
        given(mockImageStore.store(eq(mockResource), storedNameCaptor.capture())).willReturn("test-image-url");

        Images mockImage = mock(Images.class);
        given(mockImage.getId()).willReturn(1L);
        ArgumentCaptor<List<Images>> imagesCaptor = ArgumentCaptor.forClass(List.class);
        given(mockImagesRepository.saveAll(imagesCaptor.capture())).willReturn(List.of(mockImage));

        // when
        List<Long> imageIds = imageService.saveImages("username", resources);

        // then
        String storedName = storedNameCaptor.getValue();
        int extPos = storedName.lastIndexOf(".");
        assertThat(storedName.substring(extPos + 1)).isEqualTo("jpg");

        List<Images> images = imagesCaptor.getValue();
        assertThat(images).hasSize(1);
        assertThat(images).extracting("storedName").containsExactly(storedName);
        assertThat(images).extracting("originName").containsExactly("test-filename.jpg");
        assertThat(images).extracting("url").containsExactly("test-image-url");

        assertThat(imageIds)
                .hasSize(1)
                .containsExactly(1L);
    }

    @Test
    void validateImageIds() {
        // given
        List<Long> imageIds = List.of(1L, 2L, 3L);
        Images image1 = mock(Images.class);
        Images image2 = mock(Images.class);
        Images image3 = mock(Images.class);

        given(image1.getUseYn()).willReturn(true);
        given(image2.getUseYn()).willReturn(true);
        given(image3.getUseYn()).willReturn(true);

        given(mockImagesRepository.findAllById(imageIds)).willReturn(List.of(image1, image2, image3));

        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        given(member.getUseYn()).willReturn(true);

        given(mockMemberRepository.findByUsername(anyString())).willReturn(Optional.of(member));

        given(image1.getMember()).willReturn(member);
        given(image2.getMember()).willReturn(member);
        given(image3.getMember()).willReturn(member);

        // expected
        assertThat(imageService.validateImageIds(imageIds, "test-username")).isTrue();
    }

    @Test
    void validateImageIds_DuplicatedImageIds() {
        // given
        List<Long> imageIds = List.of(1L, 2L, 2L);

        // expected
        assertThat(imageService.validateImageIds(imageIds, "test-username")).isFalse();
    }

    @Test
    void validateImageIds_DeletedImageId() {
        // given
        List<Long> imageIds = List.of(1L, 2L, 3L);
        Images image1 = mock(Images.class);
        Images image2 = mock(Images.class);
        Images image3 = mock(Images.class);

        given(image1.getUseYn()).willReturn(true);
        given(image2.getUseYn()).willReturn(true);
        given(image3.getUseYn()).willReturn(false);

        given(mockImagesRepository.findAllById(imageIds)).willReturn(List.of(image1, image2, image3));

        // expected
        assertThat(imageService.validateImageIds(imageIds, "test-username")).isFalse();
    }

    @Test
    void validateImageIds_NotOwnedByMember() {
        // given
        List<Long> imageIds = List.of(1L, 2L, 3L);
        Images image1 = mock(Images.class);
        Images image2 = mock(Images.class);
        Images image3 = mock(Images.class);

        given(image1.getUseYn()).willReturn(true);
        given(image2.getUseYn()).willReturn(true);
        given(image3.getUseYn()).willReturn(true);

        given(mockImagesRepository.findAllById(imageIds)).willReturn(List.of(image1, image2, image3));

        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        given(member.getUseYn()).willReturn(true);

        Member otherMember = mock(Member.class);
        given(otherMember.getId()).willReturn(2L);

        given(mockMemberRepository.findByUsername(anyString())).willReturn(Optional.of(member));

        given(image1.getMember()).willReturn(member);
        given(image2.getMember()).willReturn(member);
        given(image3.getMember()).willReturn(otherMember);

        // expected
        assertThat(imageService.validateImageIds(imageIds, "test-username")).isFalse();
    }
}