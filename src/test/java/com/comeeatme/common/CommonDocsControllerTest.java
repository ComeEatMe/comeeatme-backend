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
                                        .description("인증이 필요한 요청의 경우 인증 토큰을 포함")
                        ),
                        responseFields(
                                fieldWithPath("success").description("API 응답 성공 여부"),
                                fieldWithPath("data").type(Object.class).description("응답 결과 데이터")
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
                                fieldWithPath("success").description("API 응답 성공 여부"),
                                fieldWithPath("error").description("에러 결과"),
                                fieldWithPath("error.code").description("에러코드"),
                                fieldWithPath("error.message").description("에러 메세지"),
                                fieldWithPath("error.errors").description("필드 에러가 없는 경우 빈 배열 []")
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
                                fieldWithPath("success").description("API 응답 성공 여부"),
                                fieldWithPath("success").description("API 응답 성공 여부"),
                                fieldWithPath("error").description("에러 결과"),
                                fieldWithPath("error.code").description("에러코드"),
                                fieldWithPath("error.message").description("에러 메세지"),
                                fieldWithPath("error.errors").description("요청 필드에 대한 예외 상황들"),
                                fieldWithPath("error.errors[].field").description("예외가 발생한 요청 필드"),
                                fieldWithPath("error.errors[].value").description("예외가 발생한 요청 값"),
                                fieldWithPath("error.errors[].reason").description("예외가 발생한 원인")
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
                                parameterWithName("page").description("페이지 번호. 0부터 시작. 기본값 0.").optional(),
                                parameterWithName("size").description("페이지 사이즈. 최대 100. 기본값 10").optional()
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("content").description("조회된 데이터"),
                                subsectionWithPath("pageable").description("페이지 요청 정보"),
                                fieldWithPath("first").description("첫 페이지 인지 여부"),
                                fieldWithPath("last").description("마지막 페이지 인지 여부"),
                                fieldWithPath("size").description("요청한 페이지 크기"),
                                fieldWithPath("number").description("현재 페이지 번호"),
                                subsectionWithPath("sort").description("데이터 정렬 정보"),
                                fieldWithPath("numberOfElements").description("현재 페이지 데이터 수"),
                                fieldWithPath("empty").description("현재 데이티 데이터 포함 여부")
                        )
                ));
    }
}
