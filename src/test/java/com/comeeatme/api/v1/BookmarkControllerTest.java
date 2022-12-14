package com.comeeatme.api.v1;

import com.comeeatme.common.RestDocsConfig;
import com.comeeatme.domain.account.service.AccountService;
import com.comeeatme.domain.bookmark.response.BookmarkedPostDto;
import com.comeeatme.domain.bookmark.response.PostBookmarked;
import com.comeeatme.domain.bookmark.service.BookmarkService;
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
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(RestDocsConfig.class)
@AutoConfigureRestDocs
@WebMvcTest(controllers = BookmarkController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class) })
class BookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookmarkService bookmarkService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private LikeService likeService;

    @Test
    @WithMockUser
    @DisplayName("????????? ????????? - DOCS")
    void bookmark_Docs() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(2L);

        //expected
        mockMvc.perform(put("/v1/member/bookmark/{postId}", 1L)
                        .with(csrf())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("v1-bookmark-bookmark",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("????????? ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("????????????")
                        )
                ));
        then(bookmarkService).should().bookmark(1L, 2L);
    }

    @Test
    @WithMockUser
    @DisplayName("????????? ????????? ?????? - DOCS")
    void cancelBookmark_Docs() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(2L);

        //expected
        mockMvc.perform(delete("/v1/member/bookmark/{postId}", 1L)
                        .with(csrf())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("v1-bookmark-cancel-bookmark",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("????????? ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("????????????")
                        )
                ));
        then(bookmarkService).should().cancelBookmark(1L, 2L);
    }

    @Test
    @WithMockUser
    @DisplayName("???????????? ????????? ?????? - DOCS")
    void getBookmarkedList_Docs() throws Exception {
        // given
        long memberId = 1L;
        given(accountService.getMemberId(anyString())).willReturn(memberId);

        BookmarkedPostDto bookmarkedPostDto = BookmarkedPostDto.builder()
                .id(2L)
                .imageUrls(List.of("image-url-1", "image-url-1"))
                .content("content")
                .createdAt(LocalDateTime.of(2022, 10, 30, 14, 35))
                .memberId(3L)
                .memberNickname("nickname")
                .memberImageUrl("member-image-url")
                .restaurantId(4L)
                .restaurantName("????????????")
                .build();
        given(bookmarkService.getBookmarkedPosts(any(Pageable.class), eq(memberId)))
                .willReturn(new SliceImpl<>(List.of(bookmarkedPostDto)));

        PostLiked postLiked = PostLiked.builder()
                .postId(2L)
                .liked(true)
                .build();
        given(likeService.areLiked(memberId, List.of(2L))).willReturn(List.of(postLiked));

        // expected
        mockMvc.perform(get("/v1/members/{memberId}/bookmarked", memberId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-bookmark-get-bookmarked-list",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        pathParameters(
                                parameterWithName("memberId").description("?????? ID")
                        ),
                        responseFields(
                                beneathPath("data.content[]").withSubsectionId("content"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("????????? ID"),
                                fieldWithPath("imageUrls").description("????????? ????????? URL ?????????"),
                                fieldWithPath("content").description("????????? ??????"),
                                fieldWithPath("createdAt").description("????????? ?????? ??????"),
                                fieldWithPath("liked").description("????????? ??????"),
                                fieldWithPath("bookmarked").description("????????? ??????"),
                                fieldWithPath("member.id").type(Long.class.getSimpleName())
                                        .description("????????? ????????? ?????? ID"),
                                fieldWithPath("member.nickname").description("????????? ????????? ?????? ?????????"),
                                fieldWithPath("member.imageUrl")
                                        .description("????????? ????????? ?????? ????????? ????????? URL. ?????? ?????? null").optional(),
                                fieldWithPath("restaurant.id").type(Long.class.getSimpleName())
                                        .description("????????? ????????? ID"),
                                fieldWithPath("restaurant.name").description("????????? ????????? ??????")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("???????????? ????????? ?????? - ?????? ?????? ?????? ??????")
    void getBookmarkedList_MemberIdNotEqual() throws Exception {
        // given
        long memberId = 1L;
        long myMemberId = 5L;
        given(accountService.getMemberId(anyString())).willReturn(myMemberId);

        BookmarkedPostDto bookmarkedPostDto = BookmarkedPostDto.builder()
                .id(2L)
                .imageUrls(List.of("image-url-1", "image-url-1"))
                .content("content")
                .createdAt(LocalDateTime.of(2022, 10, 30, 14, 35))
                .memberId(3L)
                .memberNickname("nickname")
                .memberImageUrl("member-image-url")
                .restaurantId(4L)
                .restaurantName("????????????")
                .build();
        given(bookmarkService.getBookmarkedPosts(any(Pageable.class), eq(memberId)))
                .willReturn(new SliceImpl<>(List.of(bookmarkedPostDto)));


        PostLiked postLiked = PostLiked.builder()
                .postId(2L)
                .liked(true)
                .build();
        given(likeService.areLiked(myMemberId, List.of(2L))).willReturn(List.of(postLiked));

        PostBookmarked postBookmarked = PostBookmarked.builder()
                .postId(2L)
                .bookmarked(true)
                .build();
        given(bookmarkService.areBookmarked(myMemberId, List.of(2L))).willReturn(List.of(postBookmarked));

        // expected
        mockMvc.perform(get("/v1/members/{memberId}/bookmarked", memberId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
        ;
    }

}