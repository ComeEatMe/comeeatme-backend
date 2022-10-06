package com.comeeatme.api.v1;

import com.comeeatme.common.RestDocsConfig;
import com.comeeatme.domain.images.service.ImageService;
import com.comeeatme.domain.post.HashTag;
import com.comeeatme.domain.post.request.PostCreate;
import com.comeeatme.domain.post.request.PostEdit;
import com.comeeatme.domain.post.service.PostService;
import com.comeeatme.error.exception.ErrorCode;
import com.comeeatme.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(RestDocsConfig.class)
@AutoConfigureRestDocs
@WebMvcTest(controllers = PostController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class) })
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @MockBean
    private ImageService imageService;

    @Test
    @WithMockUser
    @DisplayName("게시물 작성 API")
    void post_Docs() throws Exception {
        // given
        PostCreate postCreate = PostCreate.builder()
                .restaurantId(1L)
                .hashTags(Set.of(HashTag.STRONG_TASTE, HashTag.DATE))
                .imageIds(List.of(2L, 3L, 4L))
                .content("test-content")
                .build();
        given(imageService.validateImageIds(anyList(), anyString())).willReturn(true);
        given(postService.create(any(PostCreate.class), anyString())).willReturn(10L);

        // expected
        mockMvc.perform(post("/v1/posts").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .content(objectMapper.writeValueAsString(postCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber())
                .andDo(document("v1-posts-post",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        requestFields(
                                fieldWithPath("restaurantId").description("음식점 ID"),
                                fieldWithPath("hashTags").description("게시물 해시태그 리스트"),
                                fieldWithPath("imageIds").description("이미지 ID 리스트")
                                        .attributes(key("constraint").value("중복된 ID X. 본인의 이미지가 아닌 ID X.")),
                                fieldWithPath("content").description("게시물 내용")
                                        .attributes(key("constraint").value("최대 2000."))
                        ),
                        responseFields(
                                fieldWithPath("success").description("요청 성공 여부"),
                                fieldWithPath("data").description("생성된 게시물 ID")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("게시물 작성 API - 유효하지 않은 이미지 ID")
    void post_InvalidImageIds() throws Exception {
        // given
        PostCreate postCreate = PostCreate.builder()
                .restaurantId(1L)
                .hashTags(Set.of(HashTag.STRONG_TASTE, HashTag.DATE))
                .imageIds(List.of(2L, 3L, 3L))
                .content("test-content")
                .build();
        given(imageService.validateImageIds(anyList(), anyString())).willReturn(false);

        // expected
        mockMvc.perform(post("/v1/posts").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .content(objectMapper.writeValueAsString(postCreate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.INVALID_IMAGE_ID.name()));
    }

    @Test
    @WithMockUser
    @DisplayName("게시물 수정 API - 문서")
    void patch_Docs() throws Exception {
        // given
        PostEdit postEdit = PostEdit.builder()
                .restaurantId(1L)
                .hashTags(Set.of(HashTag.STRONG_TASTE, HashTag.EATING_ALON))
                .content("edited-content")
                .build();

        given(postService.isNotOwnedByMember(anyLong(), anyString())).willReturn(false);
        given(postService.edit(any(PostEdit.class), eq(2L))).willReturn(2L);

        // expected
        mockMvc.perform(patch("/v1/posts/{postId}", 2L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .content(objectMapper.writeValueAsString(postEdit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber())
                .andDo(document("v1-posts-patch",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시물 ID")
                        ),
                        requestFields(
                                fieldWithPath("restaurantId").description("게시물 음식점 ID"),
                                fieldWithPath("hashTags").description("게시물 해시태그 리스트"),
                                fieldWithPath("content").description("게시물 내용")
                                        .attributes(key("constraint").value("최대 2000."))
                        ),
                        responseFields(
                                fieldWithPath("success").description("요청 성공 여부"),
                                fieldWithPath("data").description("수정된 게시물 ID")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("게시물 수정 API - 소유하지 않은 게시물")
    void patch_NotOwned() throws Exception {
        // given
        PostEdit postEdit = PostEdit.builder()
                .restaurantId(1L)
                .hashTags(Set.of(HashTag.STRONG_TASTE, HashTag.EATING_ALON))
                .content("edited-content")
                .build();

        given(postService.isNotOwnedByMember(anyLong(), anyString())).willReturn(true);

        // expected
        mockMvc.perform(patch("/v1/posts/{postId}", 2L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .content(objectMapper.writeValueAsString(postEdit)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.ENTITY_ACCESS_DENIED.name()))
        ;
    }

}