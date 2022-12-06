package com.comeeatme.api.v1;

import com.comeeatme.common.RestDocsConfig;
import com.comeeatme.domain.account.service.AccountService;
import com.comeeatme.domain.favorite.service.FavoriteService;
import com.comeeatme.domain.image.service.ImageService;
import com.comeeatme.domain.post.Hashtag;
import com.comeeatme.domain.post.service.PostHashtagService;
import com.comeeatme.domain.restaurant.request.RestaurantSearch;
import com.comeeatme.domain.restaurant.response.RestaurantDto;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
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

    @MockBean
    private AccountService accountService;

    @MockBean
    private FavoriteService favoriteService;

    @MockBean
    private PostHashtagService postHashtagService;

    @MockBean
    private ImageService imageService;

    @Test
    @WithMockUser
    @DisplayName("음식점 제목 및 주소 검색 - DOCS")
    void searchSimple_Docs() throws Exception {
        // given
        List<RestaurantDto> content = List.of(
                RestaurantDto.builder()
                        .id(1L)
                        .name("지그재그")
                        .postCount(5)
                        .favoriteCount(10)
                        .addressName("서울 광진구 화양동 111-27")
                        .addressRoadName("서울 광진구 능동로19길 21-2")
                        .build(),
                RestaurantDto.builder()
                        .id(2L)
                        .name("모노끼")
                        .postCount(7)
                        .favoriteCount(10)
                        .addressName("")
                        .addressRoadName("야탑로")
                        .build()
        );
        given(restaurantService.search(any(Pageable.class), any(RestaurantSearch.class)))
                .willReturn(new SliceImpl<>(content));

        // expected
        mockMvc.perform(get("/v1/restaurants/simple")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("keyword", "음식점"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-restaurant-get-simple",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        requestParameters(
                                parameterWithName("keyword").description("음식점 검색 키워드")
                        ),
                        responseFields(
                                beneathPath("data.content[]").withSubsectionId("content"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("음식점 ID"),
                                fieldWithPath("name").description("음식점 이름"),
                                fieldWithPath("addressName").description("음식점 주소")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("음식점 검색 - DOCS")
    void search_Docs() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(10L);

        RestaurantDto restaurantDto = RestaurantDto.builder()
                .id(1L)
                .name("지그재그")
                .postCount(5)
                .favoriteCount(10)
                .addressName("서울 광진구 화양동 111-27")
                .addressRoadName("서울 광진구 능동로19길 21-2")
                .build();
        given(restaurantService.search(any(Pageable.class), any(RestaurantSearch.class)))
                .willReturn(new SliceImpl<>(List.of(restaurantDto)));

        given(postHashtagService.getHashtagsOfRestaurants(List.of(1L)))
                .willReturn(Map.of(1L, List.of(Hashtag.STRONG_TASTE, Hashtag.COST_EFFECTIVENESS)));

        given(imageService.getRestaurantIdToImages(List.of(1L), 2))
                .willReturn(Map.of(1L, List.of("image-url-1", "image-url-2")));

        // expected
        mockMvc.perform(get("/v1/restaurants")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("keyword", "지그재그")
                        .param("perImageNum", "3")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-restaurant-get-list",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        requestParameters(
                                parameterWithName("keyword").description("음식점 검색 키워드"),
                                parameterWithName("perImageNum").description("음식점당 이미지 개수. " +
                                        "null 일 경우 imageUrls 필드 반환 안함. 최대 10 최소 1.")
                        ),
                        responseFields(
                                beneathPath("data.content[]").withSubsectionId("content"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("음식점 ID"),
                                fieldWithPath("name").description("음식점 이름"),
                                fieldWithPath("postCount").type(Integer.class.getSimpleName())
                                        .description("음식점 게시물 개수"),
                                fieldWithPath("favoriteCount").type(Integer.class.getSimpleName())
                                        .description("음식점 즐겨찾기 개수"),
                                fieldWithPath("address.name").description("주소"),
                                fieldWithPath("address.roadName").description("도로명 주소"),
                                fieldWithPath("hashtags").description("해당 음식점 게시물의 해쉬태그"),
                                fieldWithPath("favorited").description("맛집 즐겨찾기 여부"),
                                fieldWithPath("imageUrls").description("해당 음식점 게시물 이미지")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("음식점 검색 - 이미지 X")
    void search_PerImageNumNull() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(10L);

        RestaurantDto restaurantDto = RestaurantDto.builder()
                .id(1L)
                .name("지그재그")
                .postCount(5)
                .favoriteCount(10)
                .addressName("서울 광진구 화양동 111-27")
                .addressRoadName("서울 광진구 능동로19길 21-2")
                .build();
        given(restaurantService.search(any(Pageable.class), any(RestaurantSearch.class)))
                .willReturn(new SliceImpl<>(List.of(restaurantDto)));

        given(postHashtagService.getHashtagsOfRestaurants(List.of(1L)))
                .willReturn(Map.of(1L, List.of(Hashtag.STRONG_TASTE, Hashtag.COST_EFFECTIVENESS)));

        // expected
        mockMvc.perform(get("/v1/restaurants")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("keyword", "지그재그")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        then(imageService).should(never()).getRestaurantIdToImages(anyList(), anyInt());
    }

    @Test
    @WithMockUser
    @DisplayName("음식점 상세 조회 - DOCS")
    void get_Docs() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(10L);

        RestaurantDto dto = RestaurantDto.builder()
                .id(1L)
                .name("음식점")
                .postCount(7)
                .favoriteCount(23)
                .addressName("주소")
                .addressRoadName("도로명주소")
                .build();
        given(restaurantService.get(1L)).willReturn(dto);

        given(favoriteService.isFavorite(10L, 1L)).willReturn(true);

        given(postHashtagService.getHashtagsOfRestaurant(1L))
                .willReturn(List.of(Hashtag.STRONG_TASTE, Hashtag.COST_EFFECTIVENESS));

        // expected
        mockMvc.perform(get("/v1/restaurants/{restaurantId}", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-restaurant-get",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("restaurantId").description("음식점 ID")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("음식점 ID"),
                                fieldWithPath("name").description("음식점 이름"),
                                fieldWithPath("postCount").type(Integer.class.getSimpleName())
                                        .description("음식점 게시물 개수"),
                                fieldWithPath("favoriteCount").type(Integer.class.getSimpleName())
                                        .description("음식점 즐겨찾기 개수"),
                                fieldWithPath("address.name").description("주소"),
                                fieldWithPath("address.roadName").description("도로명 주소"),
                                fieldWithPath("hashtags").description("해당 음식점 게시물의 해쉬태그"),
                                fieldWithPath("favorited").description("맛집 즐겨찾기 여부")
                        )
                ));
    }
}