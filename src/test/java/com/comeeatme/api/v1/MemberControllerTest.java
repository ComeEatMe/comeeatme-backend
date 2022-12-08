package com.comeeatme.api.v1;

import com.comeeatme.common.RestDocsConfig;
import com.comeeatme.security.account.service.AccountService;
import com.comeeatme.domain.common.response.DeleteResult;
import com.comeeatme.domain.common.response.DuplicateResult;
import com.comeeatme.domain.common.response.UpdateResult;
import com.comeeatme.domain.image.service.ImageService;
import com.comeeatme.domain.member.request.MemberEdit;
import com.comeeatme.domain.member.request.MemberImageEdit;
import com.comeeatme.domain.member.request.MemberSearch;
import com.comeeatme.domain.member.response.MemberDetailDto;
import com.comeeatme.domain.member.response.MemberSimpleDto;
import com.comeeatme.domain.member.service.MemberService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
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

    @Test
    @WithMockUser
    @DisplayName("회원 닉네임 중복 확인 - DOCS")
    void getNicknameDuplicate_Docs() throws Exception {
        // given
        DuplicateResult duplicateResult = DuplicateResult.builder()
                .duplicate(false)
                .build();
        given(memberService.checkNicknameDuplicate("테스트닉네임")).willReturn(duplicateResult);

        // expected
        mockMvc.perform(get("/v1/members/duplicate/nickname")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .param("nickname", "테스트닉네임"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-member-get-nickname-duplicate",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        requestParameters(
                                parameterWithName("nickname").description("중복 확인 하려는 닉네임. 최대 15.")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("duplicate").description("중복 여부. 중복이면 true.")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("회원 수정 - DOCS")
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
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        requestFields(
                                fieldWithPath("nickname").description("회원 닉네임")
                                        .attributes(key("constraint").value("최대 15. 공백 불가능.")),
                                fieldWithPath("introduction").description("회원 소개")
                                        .attributes(key("constraint").value("최대 100. 없을 경우 빈 문자열."))
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("수정된 회원 ID")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("회원 이미지 수정 - DOCS")
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
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        requestFields(
                                fieldWithPath("imageId").type(Long.class.getSimpleName())
                                        .description("회원 이미지 ID.")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("수정된 회원 ID")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("회원 이미지 수정 - 본인 이미지 X")
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
    @DisplayName("회원 이미지 삭제 - DOCS")
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
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("수정된 회원 ID")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("회원 리스트 조회 - DOCS")
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
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        requestParameters(
                                parameterWithName("nickname").description("검색할 닉네임. 최대 15. 최소 1.")
                        ),
                        responseFields(
                                beneathPath("data.content[]").withSubsectionId("content"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("회원 아이디"),
                                fieldWithPath("nickname").description("회원 닉네임"),
                                fieldWithPath("imageUrl").optional().description("회원 이미지 URL. 없을 경우 null")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("회원 상세 조회 - DOCS")
    void get_Docs() throws Exception {
        // given
        MemberDetailDto dto = MemberDetailDto.builder()
                .id(1L)
                .nickname("맛집러")
                .introduction("자기소개")
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
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("memberId").description("회원 ID")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("회원 아이디"),
                                fieldWithPath("nickname").description("회원 닉네임"),
                                fieldWithPath("introduction").description("회원 소개"),
                                fieldWithPath("imageUrl").optional().description("회원 이미지 URL. 없을 경우 null")
                        )
                ))
        ;
    }

}