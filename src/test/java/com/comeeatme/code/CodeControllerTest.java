package com.comeeatme.code;

import com.comeeatme.code.config.EnumMapperConfig;
import com.comeeatme.code.config.EnumMapperFactory;
import com.comeeatme.code.controller.CodeController;
import com.comeeatme.domain.address.response.AddressCodeDto;
import com.comeeatme.domain.address.service.AddressCodeService;
import com.comeeatme.domain.common.core.EnumMapperType;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(EnumMapperConfig.class)
@AutoConfigureRestDocs
@WebMvcTest(controllers = CodeController.class)
class CodeControllerTest {

    private static final String CODE_TYPES_PARAM = "codeTypes";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EnumMapperFactory enumMapperFactory;

    @MockBean
    private AddressCodeService addressCodeService;

    @Test
    @DisplayName("?????? ????????? ?????? API")
    @WithMockUser
    void codeApi() throws Exception {
        // given
        enumMapperFactory.put(CodeEnum.class.getSimpleName(), CodeEnum.class);

        // expected
        mockMvc.perform(get("/code")
                        .accept(MediaType.APPLICATION_JSON)
                        .param(CODE_TYPES_PARAM, "CodeEnum"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("code-api",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                            parameterWithName("codeTypes").description("??????????????? ?????? ?????????(?????????)")
                        ),
                        responseFields(
                                fieldWithPath("CodeEnum").description("?????? ??????"),
                                fieldWithPath("CodeEnum[].code").description("??????"),
                                fieldWithPath("CodeEnum[].title").description("?????? ??????")
                        )
                ));
    }

    /**
     * ?????? (EnumMapperType) ??? ????????? ?????? ???????????? ??????
     */
    @Test
    @DisplayName("?????? ????????? ?????? ?????? API")
    @WithMockUser
    void codeApi_GetAll() throws Exception {
        // given

        // expected
        mockMvc.perform(get("/code")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                //.andExpect(jsonPath("$.EnumCodeExample").isNotEmpty())  // ????????? EnumMapperType
                .andExpect(jsonPath("$.HashtagGroup").isNotEmpty())
                .andExpect(jsonPath("$.Hashtag").isNotEmpty())
                .andExpect(jsonPath("$.NoticeType").isNotEmpty())
                .andExpect(jsonPath("$.MemberDeleteReason").isNotEmpty())
                .andDo(document("code-api-get-all",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                //subsectionWithPath("FieldCategory").description("?????? ????????????")    // RestDocs ??????
                                subsectionWithPath("HashtagGroup").description("???????????? ??????"),
                                subsectionWithPath("Hashtag").description("????????????"),
                                subsectionWithPath("ReportReason").description("????????????"),
                                subsectionWithPath("NoticeType").description("???????????? ??????"),
                                subsectionWithPath("MemberDeleteReason").description("?????? ?????? ??????")
                        )
                ));
    }

    @Test
    @DisplayName("?????? ????????? ?????? API - ???????????? ?????? ?????? ??????")
    @WithMockUser
    void codeApi_CodeTypeNotExists() throws Exception {
        // given

        // expected
        mockMvc.perform(get("/code")
                        .accept(MediaType.APPLICATION_JSON)
                        .param(CODE_TYPES_PARAM, "NotFoundCode1", "NotFoundCode2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.NotFoundCode1").isEmpty())
                .andExpect(jsonPath("$.NotFoundCode2").isEmpty())
                .andDo(document("code-api-not-found"));
    }

    @RequiredArgsConstructor
    enum CodeEnum implements EnumMapperType {
        CODE("?????? ??????")
        ;

        private final String title;

        @Override
        public String getCode() {
            return name();
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    @Test
    @DisplayName("????????? ?????? ?????? - DOCS")
    @WithMockUser
    void getAddressCodeList_Docs() throws Exception {
        // given
        List<AddressCodeDto> addressCodes = List.of(
                AddressCodeDto.builder()
                        .code("4113510700")
                        .name("?????????")
                        .terminal(true)
                        .build(),
                AddressCodeDto.builder()
                        .code("4113510300")
                        .name("?????????")
                        .terminal(true)
                        .build()
        );
        given(addressCodeService.getListOfParentCode("4113500000"))
                .willReturn(addressCodes);

        // expected
        mockMvc.perform(get("/code/address/{parentCode}", "4113500000")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("code-get-address-code-list",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        pathParameters(
                                parameterWithName("parentCode").description("?????? ?????? ??????. " +
                                        "????????? ????????? ????????? ?????? ?????? ??? ?????? ??????(/code/address).")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("code").description("?????? ??????"),
                                fieldWithPath("name").description("?????? ??????"),
                                fieldWithPath("terminal").description("????????? ?????? ?????? ??????. " +
                                        "true ?????? ?????? ?????? ????????? ?????? ?????? ????????? ??????. " +
                                        "false ?????? ?????? ?????? ????????? ?????? ?????? ????????? ??????.")
                        )
                ));
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ????????? ?????? ??????")
    @WithMockUser
    void getAddressCodeList_ParentCodeNull() throws Exception {
        // given
        List<AddressCodeDto> addressCodes = List.of(
                AddressCodeDto.builder()
                        .code("1100000000")
                        .name("???????????????")
                        .terminal(true)
                        .build()
        );
        given(addressCodeService.getListOfParentCode(null))
                .willReturn(addressCodes);

        // expected
        mockMvc.perform(get("/code/address")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("code-get-address-code-list-parent-null"));
    }

}