package com.comeeatme.api.v1;

import com.comeeatme.common.RestDocsConfig;
import com.comeeatme.domain.account.service.AccountService;
import com.comeeatme.domain.common.response.CreateResults;
import com.comeeatme.domain.image.response.RestaurantImage;
import com.comeeatme.domain.image.service.ImageService;
import com.comeeatme.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(RestDocsConfig.class)
@AutoConfigureRestDocs
@WebMvcTest(controllers = ImageController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class) })
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageService imageService;

    @MockBean
    private AccountService accountService;

    @Test
    @WithMockUser
    @DisplayName("????????? ????????? ?????? - DOCS")
    void postScaled() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(10L);
        MockMultipartFile image1 = new MockMultipartFile("images", "test-image1.jpg",
                MediaType.IMAGE_JPEG_VALUE, "test-image1-content".getBytes());
        MockMultipartFile image2 = new MockMultipartFile("images", "test-image2.jpg",
                MediaType.IMAGE_JPEG_VALUE, "test-image2-content".getBytes());
        given(imageService.saveImages(any(List.class), eq(10L)))
                .willReturn(new CreateResults<>(List.of(1L, 2L)));

        // expected
        mockMvc.perform(multipart("/v1/images/scaled")
                        .file(image1)
                        .file(image2)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-image-post-scaled",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        requestParts(
                                partWithName("images").description("????????? ?????? ?????????")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("ids").description("????????? ????????? ID ?????????")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("????????? ????????? ?????? - DOCS")
    void getRestaurantImages_Docs() throws Exception {
        List<RestaurantImage> content = List.of(RestaurantImage.builder()
                .restaurantId(1L)
                .postId(2L)
                .imageUrl("image-url")
                .build());
        SliceImpl<RestaurantImage> slice = new SliceImpl<>(content);
        given(imageService.getRestaurantImages(eq(1L), any(Pageable.class))).willReturn(slice);

        // expected
        mockMvc.perform(get("/v1/restaurants/{restaurantId}/images", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-image-get-restaurant-images",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        pathParameters(
                                parameterWithName("restaurantId").description("????????? ID")
                        ),
                        responseFields(
                                beneathPath("data.content[]").withSubsectionId("content"),
                                fieldWithPath("restaurantId").type(Long.class.getSimpleName()).description("????????? ID"),
                                fieldWithPath("postId").type(Long.class.getSimpleName()).description("????????? ID"),
                                fieldWithPath("imageUrl").description("????????? URL")
                        )
                ))
                ;
    }

}