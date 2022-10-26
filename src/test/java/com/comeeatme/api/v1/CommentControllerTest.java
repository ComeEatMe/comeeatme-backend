package com.comeeatme.api.v1;

import com.comeeatme.common.RestDocsConfig;
import com.comeeatme.domain.comment.request.CommentCreate;
import com.comeeatme.domain.comment.request.CommentEdit;
import com.comeeatme.domain.comment.response.CommentDto;
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

    @Test
    @WithMockUser
    @DisplayName("댓글 작성 - DOCS")
    void post_Docs() throws Exception {
        // given
        CommentCreate commentCreate = CommentCreate.builder()
                .parentId(null)
                .content("test-content")
                .build();
        given(commentService.create(any(CommentCreate.class), anyString(), anyLong()))
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
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("댓글의 게시물 ID")
                        ),
                        requestFields(
                                fieldWithPath("parentId").type(Long.class.getSimpleName()).optional()
                                        .description("부모 댓글 ID. 대댓글이 아닐 경우 null"),
                                fieldWithPath("content").description("댓글 내용")
                                        .attributes(key("constraint").value("최대 1000."))
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").type(Long.class.getSimpleName())
                                        .description("생성된 댓글 ID")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("댓글 수정 - DOCS")
    void patch_Docs() throws Exception {
        // given
        CommentEdit commentEdit = CommentEdit.builder()
                .content("edited-content")
                .build();

        given(commentService.isNotOwnedByMember(anyLong(), anyString())).willReturn(false);
        given(commentService.isNotBelongToPost(anyLong(), anyLong())).willReturn(false);
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
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("댓글의 게시물 ID"),
                                parameterWithName("commentId").description("댓글 ID")
                        ),
                        requestFields(
                                fieldWithPath("content").description("댓글 내용")
                                        .attributes(key("constraint").value("최대 1000."))
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("수정된 댓글 ID")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("댓글 수정 - 소유하지 않은 댓글")
    void patch_NotOwned() throws Exception {
        // given
        CommentEdit commentEdit = CommentEdit.builder()
                .content("edited-content")
                .build();

        given(commentService.isNotOwnedByMember(anyLong(), anyString())).willReturn(true);

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
    @DisplayName("댓글 수정 - 잘못된 게시글 ID")
    void patch_BadPostId() throws Exception {
        // given
        CommentEdit commentEdit = CommentEdit.builder()
                .content("edited-content")
                .build();

        given(commentService.isNotOwnedByMember(anyLong(), anyString())).willReturn(false);
        given(commentService.isNotBelongToPost(anyLong(), anyLong())).willReturn(true);

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
    @DisplayName("댓글 삭제 - DOCS")
    void delete_Docs() throws Exception {
        // given
        given(commentService.isNotOwnedByMember(anyLong(), anyString())).willReturn(false);
        given(commentService.isNotBelongToPost(anyLong(), anyLong())).willReturn(false);
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
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("댓글의 게시물 ID"),
                                parameterWithName("commentId").description("댓글 ID")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("삭제된 댓글 ID")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("댓글 삭제 - 소유하지 않은 댓글")
    void delete_NotOwned() throws Exception {
        // given
        given(commentService.isNotOwnedByMember(anyLong(), anyString())).willReturn(true);

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
    @DisplayName("댓글 삭제 - 잘못된 게시글 ID")
    void delete_BadPostId() throws Exception {
        // given
        given(commentService.isNotOwnedByMember(anyLong(), anyString())).willReturn(false);
        given(commentService.isNotBelongToPost(anyLong(), anyLong())).willReturn(true);

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
    @DisplayName("댓글 리스트 조회 - DOCS")
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
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("댓글의 게시물 ID")
                        ),
                        responseFields(
                                beneathPath("data.content[]").withSubsectionId("content"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("댓글 ID"),
                                fieldWithPath("parentId").type(Long.class.getSimpleName()).optional()
                                        .description("부모 댓글 ID. 최상위 댓글(대댓글 X)인 경우 null."),
                                fieldWithPath("deleted").description("삭제 여부"),
                                fieldWithPath("content").optional().description("댓글 내용. 삭제된 경우 null."),
                                fieldWithPath("createdAt").optional().description("댓글 생성 시점. 삭제된 경우 null."),
                                fieldWithPath("member.id").type(Long.class.getSimpleName()).optional()
                                        .description("댓글 작성자 회원 ID. 삭제된 경우 null."),
                                fieldWithPath("member.nickname").optional()
                                        .description("댓글 작성자 회원 닉네임. 삭제된 경우 null."),
                                fieldWithPath("member.imageUrl").optional()
                                        .description("댓글 작성자 회원 프로필 이미지 URL. " +
                                                "프로필 이미지가 없는 경우 혹은 댓글이 삭제된 경우 null")
                        )
                ))
        ;
    }

}