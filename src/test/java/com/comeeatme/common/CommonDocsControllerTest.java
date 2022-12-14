package com.comeeatme.common;

import com.comeeatme.security.SecurityConfig;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({RestDocsConfig.class})
@WebMvcTest(controllers = CommonDocsController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class) })
@AutoConfigureRestDocs
class CommonDocsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void apiResult_Success() throws Exception {
        mockMvc.perform(get("/docs/common/api-result/success")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("common-ApiResult-success",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("????????? ????????? ????????? ?????? ?????? ????????? ??????")
                        ),
                        responseFields(
                                fieldWithPath("success").description("API ?????? ?????? ??????"),
                                fieldWithPath("data").type(Object.class).description("?????? ?????? ?????????")
                        )
                ));
    }

    @Test
    @WithMockUser
    void apiResult_Error() throws Exception {
        mockMvc.perform(get("/docs/common/api-result/error")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andDo(print())
                .andDo(document("common-ApiResult-error",
                        responseFields(
                                fieldWithPath("success").description("API ?????? ?????? ??????"),
                                fieldWithPath("error").description("?????? ??????"),
                                fieldWithPath("error.code").description("????????????"),
                                fieldWithPath("error.message").description("?????? ?????????"),
                                fieldWithPath("error.errors").description("?????? ????????? ?????? ?????? ??? ?????? []")
                        )
                ));
    }

    @Test
    @WithMockUser
    void apiResult_FieldError() throws Exception {
        mockMvc.perform(get("/docs/common/api-result/field-error")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(document("common-ApiResult-field-error",
                        responseFields(
                                fieldWithPath("success").description("API ?????? ?????? ??????"),
                                fieldWithPath("success").description("API ?????? ?????? ??????"),
                                fieldWithPath("error").description("?????? ??????"),
                                fieldWithPath("error.code").description("????????????"),
                                fieldWithPath("error.message").description("?????? ?????????"),
                                fieldWithPath("error.errors").description("?????? ????????? ?????? ?????? ?????????"),
                                fieldWithPath("error.errors[].field").description("????????? ????????? ?????? ??????"),
                                fieldWithPath("error.errors[].value").description("????????? ????????? ?????? ???"),
                                fieldWithPath("error.errors[].reason").description("????????? ????????? ??????")
                        )
                ));
    }

    @Test
    @WithMockUser
    void apiResult_Slice() throws Exception {
        mockMvc.perform(get("/docs/common/api-result/slice")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "10")
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("common-ApiResult-slice",
                        requestParameters(
                                parameterWithName("page").description("????????? ??????. 0?????? ??????. ????????? 0.").optional(),
                                parameterWithName("size").description("????????? ?????????. ?????? 100. ????????? 10").optional()
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("content").description("????????? ?????????"),
                                subsectionWithPath("pageable").description("????????? ?????? ??????"),
                                fieldWithPath("first").description("??? ????????? ?????? ??????"),
                                fieldWithPath("last").description("????????? ????????? ?????? ??????"),
                                fieldWithPath("size").description("????????? ????????? ??????"),
                                fieldWithPath("number").description("?????? ????????? ??????"),
                                subsectionWithPath("sort").description("????????? ?????? ??????"),
                                fieldWithPath("numberOfElements").description("?????? ????????? ????????? ???"),
                                fieldWithPath("empty").description("?????? ????????? ????????? ?????? ??????")
                        )
                ));
    }
}
