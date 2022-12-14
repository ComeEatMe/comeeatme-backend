package com.comeeatme.api.v1;

import com.comeeatme.common.RestDocsConfig;
import com.comeeatme.domain.account.service.AccountService;
import com.comeeatme.domain.bookmark.service.BookmarkService;
import com.comeeatme.domain.comment.service.CommentService;
import com.comeeatme.domain.common.response.CreateResult;
import com.comeeatme.domain.common.response.DeleteResult;
import com.comeeatme.domain.common.response.DuplicateResult;
import com.comeeatme.domain.common.response.UpdateResult;
import com.comeeatme.domain.favorite.service.FavoriteService;
import com.comeeatme.domain.image.service.ImageService;
import com.comeeatme.domain.like.service.LikeService;
import com.comeeatme.domain.member.Agreement;
import com.comeeatme.domain.member.MemberDeleteReason;
import com.comeeatme.domain.member.request.*;
import com.comeeatme.domain.member.response.MemberDetailDto;
import com.comeeatme.domain.member.response.MemberSimpleDto;
import com.comeeatme.domain.member.service.MemberNicknameCreator;
import com.comeeatme.domain.member.service.MemberService;
import com.comeeatme.domain.post.service.PostService;
import com.comeeatme.error.exception.ErrorCode;
import com.comeeatme.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
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

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(RestDocsConfig.class)
@AutoConfigureRestDocs
@WebMvcTest(controllers = MemberController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class) })
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private ImageService imageService;

    @MockBean
    private MemberNicknameCreator memberNicknameCreator;

    @MockBean
    private PostService postService;

    @MockBean
    private CommentService commentService;

    @MockBean
    private FavoriteService favoriteService;

    @MockBean
    private BookmarkService bookmarkService;

    @MockBean
    private LikeService likeService;

    @Test
    @WithMockUser
    @DisplayName("?????? ????????? ?????? ?????? - DOCS")
    void getNicknameDuplicate_Docs() throws Exception {
        // given
        DuplicateResult duplicateResult = DuplicateResult.builder()
                .duplicate(false)
                .build();
        given(memberService.checkNicknameDuplicate("??????????????????")).willReturn(duplicateResult);

        // expected
        mockMvc.perform(get("/v1/members/duplicate/nickname")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .param("nickname", "??????????????????"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-member-get-nickname-duplicate",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        requestParameters(
                                parameterWithName("nickname").description("?????? ?????? ????????? ?????????. ?????? 15.")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("duplicate").description("?????? ??????. ???????????? true.")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("?????? ?????? - DOCS")
    void patch_Docs() throws Exception {
        // given
        MemberEdit memberEdit = MemberEdit.builder()
                .nickname("nickname")
                .introduction("introduction")
                .build();
        given(memberService.edit(any(MemberEdit.class), anyLong())).willReturn(new UpdateResult<>(2L));

        // expected
        mockMvc.perform(patch("/v1/member")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .content(objectMapper.writeValueAsString(memberEdit)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-member-patch",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        requestFields(
                                fieldWithPath("nickname").description("?????? ?????????")
                                        .attributes(key("constraint").value("?????? 15. ?????? ?????????.")),
                                fieldWithPath("introduction").description("?????? ??????")
                                        .attributes(key("constraint").value("?????? 100. ?????? ?????? ??? ?????????."))
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
    @DisplayName("?????? ????????? ?????? - DOCS")
    void patchImage_Docs() throws Exception {
        // given
        MemberImageEdit memberImageEdit = MemberImageEdit.builder().imageId(3L).build();

        given(accountService.getMemberId(anyString())).willReturn(2L);
        given(imageService.isNotOwnedByMember(2L, 3L)).willReturn(false);
        given(memberService.editImage(anyLong(), anyLong())).willReturn(new UpdateResult<>(2L));

        // expected
        mockMvc.perform(patch("/v1/member/image")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .content(objectMapper.writeValueAsString(memberImageEdit)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-member-patch-image",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        requestFields(
                                fieldWithPath("imageId").type(Long.class.getSimpleName())
                                        .description("?????? ????????? ID.")
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
    @DisplayName("?????? ????????? ?????? - ?????? ????????? X")
    void patchImage_NotOwnedImage() throws Exception {
        // given
        MemberImageEdit memberImageEdit = MemberImageEdit.builder().imageId(3L).build();

        given(accountService.getMemberId(anyString())).willReturn(2L);
        given(imageService.isNotOwnedByMember(2L, 3L)).willReturn(true);

        // expected
        mockMvc.perform(patch("/v1/member/image")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .content(objectMapper.writeValueAsString(memberImageEdit)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.ENTITY_ACCESS_DENIED.name()))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("?????? ????????? ?????? - DOCS")
    void deleteImage_Docs() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(2L);
        given(memberService.deleteImage(anyLong())).willReturn(new DeleteResult<>(2L));

        // expected
        mockMvc.perform(delete("/v1/member/image")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-member-delete-image",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
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
    @DisplayName("?????? ????????? ?????? - DOCS")
    void getList_Docs() throws Exception {
        // given
        MemberSimpleDto memberSimpleDto = MemberSimpleDto.builder()
                .id(1L)
                .nickname("member-nickname")
                .imageUrl("member-image-url")
                .build();
        given(memberService.search(any(Pageable.class), any(MemberSearch.class)))
                .willReturn(new SliceImpl<>(List.of(memberSimpleDto)));

        // expected
        mockMvc.perform(get("/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .param("nickname", "search-nickname"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-member-get-list",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        requestParameters(
                                parameterWithName("nickname").description("????????? ?????????. ?????? 15. ?????? 1.")
                        ),
                        responseFields(
                                beneathPath("data.content[]").withSubsectionId("content"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("?????? ?????????"),
                                fieldWithPath("nickname").description("?????? ?????????"),
                                fieldWithPath("imageUrl").optional().description("?????? ????????? URL. ?????? ?????? null")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("?????? ?????? ?????? - DOCS")
    void get_Docs() throws Exception {
        // given
        MemberDetailDto dto = MemberDetailDto.builder()
                .id(1L)
                .nickname("?????????")
                .introduction("????????????")
                .imageUrl("image-url")
                .build();
        given(memberService.get(1L)).willReturn(dto);

        // expected
        mockMvc.perform(get("/v1/members/{memberId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-member-get",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        pathParameters(
                                parameterWithName("memberId").description("?????? ID")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("?????? ?????????"),
                                fieldWithPath("nickname").description("?????? ?????????"),
                                fieldWithPath("introduction").description("?????? ??????"),
                                fieldWithPath("imageUrl").optional().description("?????? ????????? URL. ?????? ?????? null")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("?????? ?????? ????????? ?????? ?????? - DOCS")
    void getAgreements() throws Exception {
        // expected
        mockMvc.perform(get("/v1/signup")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.agreements[0].code").value(Agreement.TERMS_OF_SERVICE.name()))
                .andDo(document("v1-member-get-agreements",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("agreements[].code").description("????????? ??????"),
                                fieldWithPath("agreements[].title").description("????????? ??????"),
                                fieldWithPath("agreements[].required").description("?????? ?????? ??????"),
                                fieldWithPath("agreements[].link").description("????????? ?????? ??????")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("?????? ??????- DOCS")
    void signup() throws Exception {
        // given
        MemberSignup memberSignup = MemberSignup.builder()
                .agreeOrNot(
                        Arrays.stream(Agreement.values())
                                .collect(Collectors.toMap(Function.identity(), agreement -> true))
                )
                .build();

        given(memberNicknameCreator.create()).willReturn("????????? ?????????");
        given(memberService.checkNicknameDuplicate("????????? ?????????")).willReturn(new DuplicateResult(false));
        given(memberService.create("????????? ?????????")).willReturn(new CreateResult<>(1L));

        // expected
        mockMvc.perform(post("/v1/signup").with(csrf())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberSignup))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-member-signup",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        requestFields(
                                subsectionWithPath("agreeOrNot")
                                        .description("????????? ????????? ?????? ?????? ??????. " +
                                                "key ??? ????????? code ???, value ??? ?????? ??????.")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").description("????????? ?????? ID (memberId)")
                        )
                ))
        ;
        then(accountService).should().signupMember(anyString(), eq(1L));
    }

    @Test
    @WithMockUser
    @DisplayName("?????? ?????? - DOCS")
    void delete_Docs() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(1L);
        given(memberService.delete(1L)).willReturn(new DeleteResult<>(1L));

        // expected
        mockMvc.perform(delete("/v1/member").with(csrf())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-member-delete",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").description("????????? ?????? ID")
                        )
                ))
        ;
        then(postService).should().deleteAllOfMember(1L);
        then(imageService).should().deleteAllOfMember(1L);
        then(commentService).should().deleteAllOfMember(1L);
        then(favoriteService).should().deleteAllOfMember(1L);
        then(bookmarkService).should().deleteAllOfMember(1L);
        then(likeService).should().deleteAllOfMember(1L);
        then(accountService).should().delete(anyString());

        InOrder inOrder = inOrder(postService,
                imageService,
                commentService,
                favoriteService,
                bookmarkService,
                likeService,
                accountService);
        inOrder.verify(postService).deleteAllOfMember(1L);
        inOrder.verify(imageService).deleteAllOfMember(1L);
        inOrder.verify(commentService).deleteAllOfMember(1L);
        inOrder.verify(favoriteService).deleteAllOfMember(1L);
        inOrder.verify(bookmarkService).deleteAllOfMember(1L);
        inOrder.verify(likeService).deleteAllOfMember(1L);
        inOrder.verify(accountService).delete(anyString());
    }

    @Test
    @WithMockUser
    @DisplayName("?????? ?????? ?????? ?????? - DOCS")
    void postDeleteReason_Docs() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(1L);

        MemberDelete memberDelete = MemberDelete.builder().reason(MemberDeleteReason.NO_INFORMATION).build();

        // expected
        mockMvc.perform(post("/v1/member/delete-reason").with(csrf())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberDelete))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-member-post-delete-reason",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        requestFields(
                                fieldWithPath("reason").type(MemberDeleteReason.class.getSimpleName())
                                        .description("?????? ?????? ?????? ??????")
                        )
                ))
        ;
        then(memberService).should().registerDeleteReason(1L, MemberDeleteReason.NO_INFORMATION);
    }
}