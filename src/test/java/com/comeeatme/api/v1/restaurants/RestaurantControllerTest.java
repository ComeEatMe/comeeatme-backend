package com.comeeatme.api.v1.restaurants;

import com.comeeatme.common.RestDocsConfig;
import com.comeeatme.domain.restaurant.response.RestaurantSimpleDto;
import com.comeeatme.domain.restaurant.service.RestaurantService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(RestDocsConfig.class)
@AutoConfigureRestDocs
@WebMvcTest(controllers = RestaurantController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class) })
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantService restaurantService;

    @Test
    @WithMockUser
    @DisplayName("음식점 제목 및 주소 리스트 조회 API")
    void getSimpleList_Docs() throws Exception {
        // given
        List<RestaurantSimpleDto> content = List.of(
                RestaurantSimpleDto.builder()
                        .id(1L)
                        .name("음식점1")
                        .addressName("주소1")
                        .build(),
                RestaurantSimpleDto.builder()
                        .id(2L)
                        .name("음식점2")
                        .addressName("주소2")
                        .build()
        );
        SliceImpl<RestaurantSimpleDto> slice = new SliceImpl<>(content, PageRequest.of(0, 10), false);
        given(restaurantService.getSimpleList(any(Pageable.class), anyString())).willReturn(slice);

        // expected
        mockMvc.perform(get("/v1/restaurants/simple")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("name", "음식점"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(print())
                .andDo(document("v1-restaurants-get-simple",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        requestParameters(
                                parameterWithName("name").description("검색하려는 음식점 이름")
                        ),
                        responseFields(
                                beneathPath("data.content[]").withSubsectionId("content"),
                                fieldWithPath("id").description("음식점 ID"),
                                fieldWithPath("name").description("음식점 이름"),
                                fieldWithPath("addressName").description("음식점 주소")
                        )
                ));
    }
}