package com.comeeatme.api.v1;

import com.comeeatme.common.RestDocsConfig;
import com.comeeatme.domain.account.service.AccountService;
import com.comeeatme.domain.favorite.response.FavoriteRestaurantDto;
import com.comeeatme.domain.favorite.response.RestaurantFavorited;
import com.comeeatme.domain.favorite.service.FavoriteService;
import com.comeeatme.domain.image.service.ImageService;
import com.comeeatme.domain.post.Hashtag;
import com.comeeatme.domain.post.service.PostHashtagService;
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

    @MockBean
    private ImageService imageService;

    @MockBean
    private PostHashtagService postHashtagService;

    @Test
    @WithMockUser
    @DisplayName("?????? ???????????? - DOCS")
    void put_Docs() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(2L);

        // expected
        mockMvc.perform(put("/v1/member/favorite/{restaurantId}", 1L)
                        .with(csrf())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andExpect(status().isOk())
                .andDo(document("v1-favorite-put",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        pathParameters(
                                parameterWithName("restaurantId").description("????????? ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("????????????")
                        )
                ));
        then(favoriteService).should().favorite(1L, 2L);
    }

    @Test
    @WithMockUser
    @DisplayName("?????? ???????????? ?????? - DOCS")
    void delete_Docs() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(2L);

        // expected
        mockMvc.perform(delete("/v1/member/favorite/{restaurantId}", 1L)
                        .with(csrf())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andExpect(status().isOk())
                .andDo(document("v1-favorite-delete",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        pathParameters(
                                parameterWithName("restaurantId").description("????????? ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("????????????")
                        )
                ));
        then(favoriteService).should().cancelFavorite(1L, 2L);
    }

    @Test
    @WithMockUser
    @DisplayName("?????? ??????????????? ????????? ?????? - DOCS")
    void getFavoriteList_Docs() throws Exception {
        // given
        long memberId = 1L;
        long myMemberId = 5L;
        given(accountService.getMemberId(anyString())).willReturn(myMemberId);

        FavoriteRestaurantDto favoriteRestaurantDto = FavoriteRestaurantDto.builder()
                .id(2L)
                .name("????????????")
                .build();
        given(favoriteService.getFavoriteRestaurants(any(Pageable.class), eq(memberId)))
                .willReturn(new SliceImpl<>(List.of(favoriteRestaurantDto)));

        RestaurantFavorited restaurantFavorited = RestaurantFavorited.builder()
                .restaurantId(2L)
                .favorited(true)
                .build();
        given(favoriteService.areFavorite(myMemberId, List.of(2L))).willReturn(List.of(restaurantFavorited));


        given(imageService.getRestaurantIdToImages(List.of(2L), 3))
                .willReturn(Map.of(2L, List.of("image-url-1", "image-url-2")));

        given(postHashtagService.getHashtagsOfRestaurants(List.of(2L)))
                .willReturn(Map.of(2L, List.of(Hashtag.DATE, Hashtag.STRONG_TASTE)));

        // expected
        mockMvc.perform(get("/v1/members/{memberId}/favorite", memberId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("perImageNum", "3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-favorite-get-favorite-list",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        pathParameters(
                                parameterWithName("memberId").description("?????? ID")
                        ),
                        responseFields(
                                beneathPath("data.content[]").withSubsectionId("content"),
                                fieldWithPath("id").type(Long.class.getSimpleName())
                                        .description("????????? ID"),
                                fieldWithPath("name").description("????????? ??????"),
                                fieldWithPath("favorited").description("?????? ???????????? ?????? ??????"),
                                fieldWithPath("hashtags").description("?????? ????????? ???????????? ????????????"),
                                fieldWithPath("imageUrls").description("?????? ????????? ????????? ?????????")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("?????? ??????????????? ????????? ?????? - ?????? ?????? ??????")
    void getFavoriteList_MyFavoriteList() throws Exception {
        // given
        long memberId = 1L;
        given(accountService.getMemberId(anyString())).willReturn(memberId);

        FavoriteRestaurantDto favoriteRestaurantDto = FavoriteRestaurantDto.builder()
                .id(2L)
                .name("????????????")
                .build();
        given(favoriteService.getFavoriteRestaurants(any(Pageable.class), eq(memberId)))
                .willReturn(new SliceImpl<>(List.of(favoriteRestaurantDto)));

        // expected
        mockMvc.perform(get("/v1/members/{memberId}/favorite", memberId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
        ;
        then(favoriteService).should(never()).areFavorite(anyLong(), anyList());
    }

}