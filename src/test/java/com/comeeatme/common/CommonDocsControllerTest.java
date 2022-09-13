package com.comeeatme.common;

import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TODO ApiResult 문서화용 테스트 패키지 정리 고려 -> ApiResult 와 같은 패키지
// TODO 발생 가능한 에러를 최소화하고 에러 문서화를 따로 빼는 것 고려
@Import({RestDocsConfig.class})
@WebMvcTest(CommonDocsController.class)
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
}
