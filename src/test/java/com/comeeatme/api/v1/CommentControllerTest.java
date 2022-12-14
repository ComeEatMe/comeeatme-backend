package com.comeeatme.api.v1;

import com.comeeatme.common.RestDocsConfig;
import com.comeeatme.domain.account.service.AccountService;
import com.comeeatme.domain.comment.request.CommentCreate;
import com.comeeatme.domain.comment.request.CommentEdit;
import com.comeeatme.domain.comment.response.CommentDto;
import com.comeeatme.domain.comment.response.MemberCommentDto;
import com.comeeatme.domain.comment.service.CommentService;
import com.comeeatme.domain.common.response.CreateResult;
import com.comeeatme.domain.common.response.DeleteResult;
import com.comeeatme.domain.common.response.UpdateResult;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(RestDocsConfig.class)
@AutoConfigureRestDocs
@WebMvcTest(controllers = CommentController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)})
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private AccountService accountService;

    @Test
    @WithMockUser
    @DisplayName("?????? ?????? - DOCS")
    void post_Docs() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(3L);
        CommentCreate commentCreate = CommentCreate.builder()
                .parentId(null)
                .content("test-content")
                .build();
        given(commentService.create(any(CommentCreate.class), eq(3L), eq(2L)))
                .willReturn(new CreateResult<>(1L));

        // expected
        mockMvc.perform(post("/v1/posts/{postId}/comment", 2L).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .content(objectMapper.writeValueAsString(commentCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-comment-post",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("????????? ????????? ID")
                        ),
                        requestFields(
                                fieldWithPath("parentId").type(Long.class.getSimpleName()).optional()
                                        .description("?????? ?????? ID. ???????????? ?????? ?????? null"),
                                fieldWithPath("content").description("?????? ??????")
                                        .attributes(key("constraint").value("?????? 1000."))
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").type(Long.class.getSimpleName())
                                        .description("????????? ?????? ID")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("?????? ?????? - DOCS")
    void patch_Docs() throws Exception {
        // given
        CommentEdit commentEdit = CommentEdit.builder()
                .content("edited-content")
                .build();

        given(accountService.getMemberId(anyString())).willReturn(10L);
        given(commentService.isNotOwnedByMember(1L, 10L)).willReturn(false);
        given(commentService.isNotBelongToPost(1L, 2L)).willReturn(false);
        given(commentService.edit(any(CommentEdit.class), eq(1L))).willReturn(new UpdateResult<>(1L));

        // expected
        mockMvc.perform(patch("/v1/posts/{postId}/comments/{commentId}", 2L, 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .content(objectMapper.writeValueAsString(commentEdit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-comment-patch",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("????????? ????????? ID"),
                                parameterWithName("commentId").description("?????? ID")
                        ),
                        requestFields(
                                fieldWithPath("content").description("?????? ??????")
                                        .attributes(key("constraint").value("?????? 1000."))
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("????????? ?????? ID")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("?????? ?????? - ???????????? ?????? ??????")
    void patch_NotOwned() throws Exception {
        // given
        CommentEdit commentEdit = CommentEdit.builder()
                .content("edited-content")
                .build();

        given(accountService.getMemberId(anyString())).willReturn(10L);
        given(commentService.isNotOwnedByMember(1L, 10L)).willReturn(true);

        // expected
        mockMvc.perform(patch("/v1/posts/{postId}/comments/{commentId}", 2L, 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .content(objectMapper.writeValueAsString(commentEdit)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.ENTITY_ACCESS_DENIED.name()))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("?????? ?????? - ????????? ????????? ID")
    void patch_BadPostId() throws Exception {
        // given
        CommentEdit commentEdit = CommentEdit.builder()
                .content("edited-content")
                .build();

        given(accountService.getMemberId(anyString())).willReturn(10L);
        given(commentService.isNotOwnedByMember(1L, 10L)).willReturn(false);
        given(commentService.isNotBelongToPost(1L, 2L)).willReturn(true);

        // expected
        mockMvc.perform(patch("/v1/posts/{postId}/comments/{commentId}", 2L, 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .content(objectMapper.writeValueAsString(commentEdit)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.ENTITY_NOT_FOUND.name()))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("?????? ?????? - DOCS")
    void delete_Docs() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(10L);
        given(commentService.isNotOwnedByMember(1L, 10L)).willReturn(false);
        given(commentService.isNotBelongToPost(1L, 2L)).willReturn(false);
        given(commentService.delete(1L)).willReturn(new DeleteResult<>(1L));

        // expected
        mockMvc.perform(delete("/v1/posts/{postId}/comments/{commentId}", 2L, 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-comment-delete",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("????????? ????????? ID"),
                                parameterWithName("commentId").description("?????? ID")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("????????? ?????? ID")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("?????? ?????? - ???????????? ?????? ??????")
    void delete_NotOwned() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(10L);
        given(commentService.isNotOwnedByMember(1L, 10L)).willReturn(true);

        // expected
        mockMvc.perform(delete("/v1/posts/{postId}/comments/{commentId}", 2L, 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.ENTITY_ACCESS_DENIED.name()))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("?????? ?????? - ????????? ????????? ID")
    void delete_BadPostId() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(10L);
        given(commentService.isNotOwnedByMember(1L, 10L)).willReturn(false);
        given(commentService.isNotBelongToPost(1L, 2L)).willReturn(true);

        // expected
        mockMvc.perform(delete("/v1/posts/{postId}/comments/{commentId}", 2L, 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.ENTITY_NOT_FOUND.name()))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("?????? ????????? ?????? - DOCS")
    void get_Docs() throws Exception {
        // given
        List<CommentDto> content = List.of(
                CommentDto.builder()
                        .id(1L)
                        .parentId(2L)
                        .deleted(false)
                        .content("comment content 1")
                        .createdAt(LocalDateTime.of(2022, 3, 1, 12, 30))
                        .memberId(3L)
                        .memberNickname("nickname1")
                        .memberImageUrl("member-image-url")
                        .build(),
                CommentDto.builder()
                        .id(4L)
                        .parentId(null)
                        .deleted(false)
                        .content("comment content 2")
                        .createdAt(LocalDateTime.of(2022, 4, 1, 13, 0))
                        .memberId(6L)
                        .memberNickname("nickname2")
                        .memberImageUrl(null)
                        .build(),
                CommentDto.builder()
                        .id(7L)
                        .parentId(8L)
                        .deleted(true)
                        .build()
        );
        Slice<CommentDto> slice = new SliceImpl<>(content, PageRequest.of(0, 10), true);
        given(commentService.getListOfPost(any(Pageable.class), anyLong())).willReturn(slice);

        // expected
        mockMvc.perform(get("/v1/posts/{postId}/comments", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-comment-get",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("????????? ????????? ID")
                        ),
                        responseFields(
                                beneathPath("data.content[]").withSubsectionId("content"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("?????? ID"),
                                fieldWithPath("parentId").type(Long.class.getSimpleName()).optional()
                                        .description("?????? ?????? ID. ????????? ??????(????????? X)??? ?????? null."),
                                fieldWithPath("deleted").description("?????? ??????"),
                                fieldWithPath("content").optional().description("?????? ??????. ????????? ?????? null."),
                                fieldWithPath("createdAt").optional().description("?????? ?????? ??????. ????????? ?????? null."),
                                fieldWithPath("member.id").type(Long.class.getSimpleName()).optional()
                                        .description("?????? ????????? ?????? ID. ????????? ?????? null."),
                                fieldWithPath("member.nickname").optional()
                                        .description("?????? ????????? ?????? ?????????. ????????? ?????? null."),
                                fieldWithPath("member.imageUrl").optional()
                                        .description("?????? ????????? ?????? ????????? ????????? URL. " +
                                                "????????? ???????????? ?????? ?????? ?????? ????????? ????????? ?????? null")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("?????? ?????? ????????? ?????? - DOCS")
    void getMemberComments_Docs() throws Exception {
        // given
        MemberCommentDto comment = MemberCommentDto.builder()
                .id(10L)
                .content("comment-content")
                .createdAt(LocalDateTime.of(2022, 12, 19, 23, 56))
                .postId(20L)
                .content("post-content")
                .postImageUrl("post-image-url")
                .build();
        given(commentService.getListOfMember(any(Pageable.class), eq(1L)))
                .willReturn(new SliceImpl<>(List.of(comment)));


        // expected
        mockMvc.perform(get("/v1/members/{memberId}/comments", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-comment-get-member-comment-list",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        pathParameters(
                                parameterWithName("memberId").description("????????? ?????? ID")
                        ),
                        responseFields(
                                beneathPath("data.content[]").withSubsectionId("content"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("?????? ID"),
                                fieldWithPath("content").description("?????? ??????."),
                                fieldWithPath("createdAt").description("?????? ?????? ??????."),
                                fieldWithPath("post.id").type(Long.class.getSimpleName()).description("?????? ????????? ID."),
                                fieldWithPath("post.content").description("?????? ????????? ??????."),
                                fieldWithPath("post.imageUrl").description("?????? ????????? ????????? URL.")
                        )
                ))
        ;
    }

}