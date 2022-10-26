package com.comeeatme.api.v1;

import com.comeeatme.common.RestDocsConfig;
import com.comeeatme.domain.likes.response.LikeResult;
import com.comeeatme.domain.likes.response.LikedResult;
import com.comeeatme.domain.likes.service.LikeService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(RestDocsConfig.class)
@AutoConfigureRestDocs
@WebMvcTest(controllers = LikeController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class) })
class LikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LikeService likeService;

    @Test
    @WithMockUser
    @DisplayName("게시물 좋아요 API - 문서")
    void like_Docs() throws Exception {
        // given
        LikeResult likeResult = LikeResult.builder()
                .postId(1L)
                .liked(true)
                .count(10L)
                .build();
        given(likeService.pushLike(eq(1L), anyString())).willReturn(likeResult);

        // expected
        mockMvc.perform(put("/v1/member/like/{postId}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-member-like-put",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시물 ID")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("postId").description("게시물 ID"),
                                fieldWithPath("liked").description("좋아요 생성(true), 취소(false)"),
                                fieldWithPath("count").description("개시물 내 좋아요 개수")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("게시물 좋아요 여부 조회 - 문서")
    void getLiked_Docs() throws Exception {
        // given
        given(likeService.isLiked(List.of(1L, 2L), 3L))
                .willReturn(List.of(
                        LikedResult.builder()
                                .postId(1L)
                                .liked(true)
                                .build(),
                        LikedResult.builder()
                                .postId(2L)
                                .liked(false)
                                .build()
                ));

        // expected
        mockMvc.perform(get("/v1/members/{memberId}/liked", 3L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("postIds", "1", "2")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-members-liked-get",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        requestParameters(
                                parameterWithName("postIds")
                                        .description("조회하려는 게시물 ID 리스트. 최대 100")
                        ),
                        responseFields(
                                beneathPath("data[]").withSubsectionId("data"),
                                fieldWithPath("postId").description("게시물 ID"),
                                fieldWithPath("liked").description("좋아요 여부")
                        )
                ))
        ;
    }

}