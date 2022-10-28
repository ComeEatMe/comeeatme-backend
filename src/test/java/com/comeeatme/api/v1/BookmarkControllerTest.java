package com.comeeatme.api.v1;

import com.comeeatme.common.RestDocsConfig;
import com.comeeatme.domain.bookmark.BookmarkGroup;
import com.comeeatme.domain.bookmark.response.BookmarkGroupDto;
import com.comeeatme.domain.bookmark.service.BookmarkService;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    @WithMockUser
    @DisplayName("게시물 북마크 - DOCS")
    void bookmark_Docs() throws Exception {
        mockMvc.perform(put("/v1/member/bookmark/{groupName}/{postId}", "그루비룸", 1L)
                        .with(csrf())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("v1-bookmark-bookmark",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("groupName").description("북마크 그룹"),
                                parameterWithName("postId").description("게시물 ID")
                        )
                ));
        then(bookmarkService).should().bookmark(eq(1L), anyString(), eq("그루비룸"));
    }

    @Test
    @WithMockUser
    @DisplayName("게시물 북마크 (그룹 지정 X) - DOCS")
    void bookmark_GroupNull_Docs() throws Exception {
        mockMvc.perform(put("/v1/member/bookmark/{postId}", 1L)
                        .with(csrf())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("v1-bookmark-bookmark-group-null",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시물 ID")
                        )
                ));
        then(bookmarkService).should().bookmark(eq(1L), anyString(), eq(null));
    }

    @Test
    @WithMockUser
    @DisplayName("게시물 북마크 취소 - DOCS")
    void cancelBookmark_Docs() throws Exception {
        mockMvc.perform(delete("/v1/member/bookmark/{groupName}/{postId}", "그루비룸", 1L)
                        .with(csrf())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("v1-bookmark-cancel-bookmark",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("groupName").description("북마크 그룹"),
                                parameterWithName("postId").description("게시물 ID")
                        )
                ));
        then(bookmarkService).should().cancelBookmark(eq(1L), anyString(), eq("그루비룸"));
    }

    @Test
    @WithMockUser
    @DisplayName("게시물 북마크 취소 (그룹 지정 X) - DOCS")
    void cancelBookmark_Docs_GroupNull_Docs() throws Exception {
        mockMvc.perform(delete("/v1/member/bookmark/{postId}", 1L)
                        .with(csrf())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("v1-bookmark-cancel-bookmark-group-null",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("게시물 ID")
                        )
                ));
        then(bookmarkService).should().cancelBookmark(eq(1L), anyString(), eq(null));
    }

    @Test
    @WithMockUser
    @DisplayName("북마크 그룹 리스트 조회 - DOCS")
    void getBookmarkGroups_Docs() throws Exception {
        // given
        List<BookmarkGroupDto> groups = List.of(
                BookmarkGroupDto.builder()
                        .name(BookmarkGroup.ALL_NAME)
                        .bookmarkCount(10)
                        .build(),
                BookmarkGroupDto.builder()
                        .name("그루비룸")
                        .bookmarkCount(2)
                        .build(),
                BookmarkGroupDto.builder()
                        .name("국밥")
                        .bookmarkCount(3)
                        .build()
        );
        given(bookmarkService.getAllGroupsOfMember(1L)).willReturn(groups);

        // expected
        mockMvc.perform(get("/v1/members/{memberId}/bookmark-groups", 1L)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-bookmark-get-bookmark-groups",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("memberId").description("회원 ID")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("name").description("북마크 그룹 이름. 모든 북마크는 " +
                                        BookmarkGroup.ALL_NAME + " 그룹에 포함되어 있음."),
                                fieldWithPath("bookmarkCount").type(Integer.class.getSimpleName())
                                        .description("북마크 그룹에 포함된 북마크 개수")
                        )
                ));
    }

}