package com.comeeatme.api.v1;

import com.comeeatme.common.RestDocsConfig;
import com.comeeatme.domain.account.service.AccountService;
import com.comeeatme.domain.bookmark.response.PostBookmarked;
import com.comeeatme.domain.bookmark.service.BookmarkService;
import com.comeeatme.domain.like.response.LikedPostDto;
import com.comeeatme.domain.like.response.PostLiked;
import com.comeeatme.domain.like.service.LikeService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

    @MockBean
    private AccountService accountService;

    @MockBean
    private BookmarkService bookmarkService;

    @Test
    @WithMockUser
    @DisplayName("게시물 좋아요 - DOCS")
    void like_Docs() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(2L);

        // expected
        mockMvc.perform(put("/v1/member/like/{postId}", 1L)
                        .with(csrf())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andExpect(status().isOk())
                .andDo(document("v1-like-like",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시물 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공여부")
                        )
                ))
        ;
        then(likeService).should().like(1L, 2L);
    }

    @Test
    @WithMockUser
    @DisplayName("게시물 좋아요 취소 - DOCS")
    void unlike_Docs() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(2L);

        // expected
        mockMvc.perform(delete("/v1/member/like/{postId}", 1L)
                        .with(csrf())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andExpect(status().isOk())
                .andDo(document("v1-like-unlike",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시물 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공여부")
                        )
                ))
        ;
        then(likeService).should().unlike(1L, 2L);
    }

    @Test
    @WithMockUser
    @DisplayName("좋아요한 게시물 조회 - DOCS")
    void getLikedList() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(1L);

        LikedPostDto likedPostDto = LikedPostDto.builder()
                .id(2L)
                .content("post-content")
                .createdAt(LocalDateTime.of(2022, 10, 31, 0, 53))
                .imageUrls(List.of("image-url-1", "image-url-2"))
                .build();
        given(likeService.getLikedPosts(any(Pageable.class), eq(1L)))
                .willReturn(new SliceImpl<>(List.of(likedPostDto)));

        PostBookmarked postBookmarked = PostBookmarked.builder()
                .postId(2L)
                .bookmarked(false)
                .build();
        given(bookmarkService.areBookmarked(1L, List.of(2L)))
                .willReturn(List.of(postBookmarked));

        // expected
        mockMvc.perform(get("/v1/members/{memberId}/liked", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].liked").value(true))
                .andExpect(jsonPath("$.data.content[0].bookmarked").value(false))
                .andDo(document("v1-like-get-liked-list",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("memberId").description("회원 ID")
                        ),
                        responseFields(
                                beneathPath("data.content[]").withSubsectionId("content"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("게시물 ID"),
                                fieldWithPath("imageUrls").description("게시물 이미지 URL 리스트"),
                                fieldWithPath("content").description("게시물 내용"),
                                fieldWithPath("createdAt").description("게시물 생성 시점"),
                                fieldWithPath("liked").description("좋아요 여부"),
                                fieldWithPath("bookmarked").description("북마크 여부")
                        )
                ))
        ;
        then(likeService).should(never()).areLiked(anyLong(), anyList());
    }

    @Test
    @WithMockUser
    @DisplayName("좋아요한 게시물 조회 - 본인 이외 회원 조회")
    void getLikedList_MemberIdNotEqual() throws Exception {
        // given
        long memberId = 1L;
        long myMemberId = 5L;
        given(accountService.getMemberId(anyString())).willReturn(myMemberId);

        LikedPostDto likedPostDto = LikedPostDto.builder()
                .id(2L)
                .content("post-content")
                .createdAt(LocalDateTime.of(2022, 10, 31, 0, 53))
                .imageUrls(List.of("image-url-1", "image-url-2"))
                .build();
        given(likeService.getLikedPosts(any(Pageable.class), eq(memberId)))
                .willReturn(new SliceImpl<>(List.of(likedPostDto)));

        PostLiked postLiked = PostLiked.builder()
                .postId(2L)
                .liked(false)
                .build();
        given(likeService.areLiked(myMemberId, List.of(2L)))
                .willReturn(List.of(postLiked));

        PostBookmarked postBookmarked = PostBookmarked.builder()
                .postId(2L)
                .bookmarked(true)
                .build();
        given(bookmarkService.areBookmarked(myMemberId, List.of(2L)))
                .willReturn(List.of(postBookmarked));


        // expected
        mockMvc.perform(get("/v1/members/{memberId}/liked", memberId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].liked").value(false))
                .andExpect(jsonPath("$.data.content[0].bookmarked").value(true))
        ;
    }

}