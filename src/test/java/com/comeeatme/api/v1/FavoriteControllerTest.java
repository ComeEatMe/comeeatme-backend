package com.comeeatme.api.v1;

import com.comeeatme.common.RestDocsConfig;
import com.comeeatme.domain.account.service.AccountService;
import com.comeeatme.domain.favorite.FavoriteGroup;
import com.comeeatme.domain.favorite.response.FavoriteGroupDto;
import com.comeeatme.domain.favorite.response.FavoriteRestaurantDto;
import com.comeeatme.domain.favorite.response.RestaurantFavorited;
import com.comeeatme.domain.favorite.service.FavoriteService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(RestDocsConfig.class)
@AutoConfigureRestDocs
@WebMvcTest(controllers = FavoriteController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class) })
class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavoriteService favoriteService;

    @MockBean
    private AccountService accountService;

    @Test
    @WithMockUser
    @DisplayName("맛집 즐겨찾기 - DOCS")
    void put_Docs() throws Exception {
        mockMvc.perform(put("/v1/member/favorite/{groupName}/{restaurantId}", "그루비룸", 1L)
                        .with(csrf())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andExpect(status().isNoContent())
                .andDo(document("v1-favorite-put",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("groupName").description("맛집 즐겨찾기 그룹"),
                                parameterWithName("restaurantId").description("음식점 ID")
                        )
                ));
        then(favoriteService).should().favorite(eq(1L), anyString(), eq("그루비룸"));
    }

    @Test
    @WithMockUser
    @DisplayName("맛집 즐겨찾기 (그룹 지정 X) - DOCS")
    void put_GroupNull_Docs() throws Exception {
        mockMvc.perform(put("/v1/member/favorite/{restaurantId}", 1L)
                        .with(csrf())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andExpect(status().isNoContent())
                .andDo(document("v1-favorite-put-group-null",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("restaurantId").description("음식점 ID")
                        )
                ));
        then(favoriteService).should().favorite(eq(1L), anyString(), eq(null));
    }

    @Test
    @WithMockUser
    @DisplayName("맛집 즐겨찾기 취소 - DOCS")
    void delete_Docs() throws Exception {
        mockMvc.perform(delete("/v1/member/favorite/{groupName}/{restaurantId}", "그루비룸", 1L)
                        .with(csrf())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andExpect(status().isNoContent())
                .andDo(document("v1-favorite-delete",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("groupName").description("맛집 즐겨찾기 그룹"),
                                parameterWithName("restaurantId").description("음식점 ID")
                        )
                ));
        then(favoriteService).should().cancelFavorite(eq(1L), anyString(), eq("그루비룸"));
    }

    @Test
    @WithMockUser
    @DisplayName("맛집 즐겨찾기 취소 (그룹 지정 X) - DOCS")
    void delete_GroupNull_Docs() throws Exception {
        mockMvc.perform(delete("/v1/member/favorite/{restaurantId}", 1L)
                        .with(csrf())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andExpect(status().isNoContent())
                .andDo(document("v1-favorite-delete-group-null",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("restaurantId").description("음식점 ID")
                        )
                ));
        then(favoriteService).should().cancelFavorite(eq(1L), anyString(), eq(null));
    }

    @Test
    @WithMockUser
    @DisplayName("맛집 즐겨찾기 그룹 리스트 조회 - DOCS")
    void getFavoriteGroups() throws Exception {
        // given
        List<FavoriteGroupDto> groups = List.of(
                FavoriteGroupDto.builder()
                        .name(FavoriteGroup.ALL_NAME)
                        .favoriteCount(10)
                        .build(),
                FavoriteGroupDto.builder()
                        .name("그루비룸")
                        .favoriteCount(2)
                        .build(),
                FavoriteGroupDto.builder()
                        .name("학교 앞")
                        .favoriteCount(3)
                        .build()
        );
        given(favoriteService.getAllGroupsOfMember(1L)).willReturn(groups);

        // expected
        mockMvc.perform(get("/v1/members/{memberId}/favorite-groups", 1L)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-favorite-get-favorite-groups",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("memberId").description("회원 ID")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("name").description("맛집 즐겨찾기 그룹 이름. 모든 맛집 즐겨찾기는 " +
                                        FavoriteGroup.ALL_NAME + " 그룹에 포함되어 있음."),
                                fieldWithPath("favoriteCount").type(Integer.class.getSimpleName())
                                        .description("맛집 즐겨찾기 그룹에 포함된 즐겨찾기 개수")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("맛집 즐겨찾기된 음식점 조회 - DOCS")
    void getFavoriteList_Docs() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(1L);

        FavoriteRestaurantDto favoriteRestaurantDto = FavoriteRestaurantDto.builder()
                .id(2L)
                .name("지그재그")
                .build();
        given(favoriteService.getFavoriteRestaurants(any(Pageable.class), eq(1L), eq("그루비룸")))
                .willReturn(new SliceImpl<>(List.of(favoriteRestaurantDto)));

        // expected
        mockMvc.perform(get("/v1/members/{memberId}/favorite/{groupName}", 1L, "그루비룸")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-favorite-get-favorite-list",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("인증 필요")
                        ),
                        pathParameters(
                                parameterWithName("memberId").description("회원 ID"),
                                parameterWithName("groupName")
                                        .description("그룹 이름. 모든 그룹을 조회하려면 지정하지 않음.")
                        ),
                        responseFields(
                                beneathPath("data.content[]").withSubsectionId("content"),
                                fieldWithPath("id").type(Long.class.getSimpleName())
                                        .description("음식점 ID"),
                                fieldWithPath("name").description("음식점 이름"),
                                fieldWithPath("favorited").description("맛집 즐겨찾기 등록 여부")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("맛집 즐겨찾기된 음식점 조회 - 본인 이외 회원 조회")
    void getFavoriteList_MemberIdNotEqual() throws Exception {
        // given
        long memberId = 1L;
        long myMemberId = 5L;
        given(accountService.getMemberId(anyString())).willReturn(myMemberId);

        FavoriteRestaurantDto favoriteRestaurantDto = FavoriteRestaurantDto.builder()
                .id(2L)
                .name("지그재그")
                .build();
        given(favoriteService.getFavoriteRestaurants(any(Pageable.class), eq(memberId), eq("그루비룸")))
                .willReturn(new SliceImpl<>(List.of(favoriteRestaurantDto)));

        RestaurantFavorited restaurantFavorited = RestaurantFavorited.builder()
                .restaurantId(2L)
                .favorited(true)
                .build();
        given(favoriteService.isFavorite(myMemberId, List.of(2L))).willReturn(List.of(restaurantFavorited));

        // expected
        mockMvc.perform(get("/v1/members/{memberId}/favorite/{groupName}", memberId, "그루비룸")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("맛집 즐겨찾기된 음식점 조회 - 전체 그룹 조회")
    void getFavoriteList_GroupNull() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(1L);

        FavoriteRestaurantDto favoriteRestaurantDto = FavoriteRestaurantDto.builder()
                .id(2L)
                .name("지그재그")
                .build();
        given(favoriteService.getFavoriteRestaurants(any(Pageable.class), eq(1L), eq(null)))
                .willReturn(new SliceImpl<>(List.of(favoriteRestaurantDto)));

        // expected
        mockMvc.perform(get("/v1/members/{memberId}/favorite", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-favorite-get-favorite-list-group-null",
                        pathParameters(
                                parameterWithName("memberId").description("회원 ID")
                        )
                ))
        ;
    }

}