package com.comeeatme.api.v1;

import com.comeeatme.common.RestDocsConfig;
import com.comeeatme.domain.account.service.AccountService;
import com.comeeatme.domain.bookmark.response.PostBookmarked;
import com.comeeatme.domain.bookmark.service.BookmarkService;
import com.comeeatme.domain.common.response.CreateResult;
import com.comeeatme.domain.common.response.DeleteResult;
import com.comeeatme.domain.common.response.UpdateResult;
import com.comeeatme.domain.image.service.ImageService;
import com.comeeatme.domain.like.response.PostLiked;
import com.comeeatme.domain.like.service.LikeService;
import com.comeeatme.domain.post.Hashtag;
import com.comeeatme.domain.post.request.PostCreate;
import com.comeeatme.domain.post.request.PostEdit;
import com.comeeatme.domain.post.request.PostSearch;
import com.comeeatme.domain.post.response.MemberPostDto;
import com.comeeatme.domain.post.response.PostDetailDto;
import com.comeeatme.domain.post.response.PostDto;
import com.comeeatme.domain.post.response.RestaurantPostDto;
import com.comeeatme.domain.post.service.PostService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(RestDocsConfig.class)
@AutoConfigureRestDocs
@WebMvcTest(controllers = PostController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class) })
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @MockBean
    private ImageService imageService;

    @MockBean
    private LikeService likeService;

    @MockBean
    private BookmarkService bookmarkService;

    @MockBean
    private AccountService accountService;


    @Test
    @WithMockUser
    @DisplayName("????????? ?????? - DOCS")
    void post_Docs() throws Exception {
        // given
        PostCreate postCreate = PostCreate.builder()
                .restaurantId(1L)
                .hashtags(Set.of(Hashtag.STRONG_TASTE, Hashtag.DATE))
                .imageIds(List.of(2L, 3L, 4L))
                .content("test-content")
                .build();
        given(accountService.getMemberId(anyString())).willReturn(10L);
        given(imageService.validateImageIds(anyList(), eq(10L))).willReturn(true);
        given(postService.create(any(PostCreate.class), eq(10L))).willReturn(new CreateResult<>(10L));

        // expected
        mockMvc.perform(post("/v1/post").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .content(objectMapper.writeValueAsString(postCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-post-post",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        requestFields(
                                fieldWithPath("restaurantId").type(Long.class.getSimpleName()).description("????????? ID"),
                                fieldWithPath("hashtags").description("????????? ???????????? ?????????"),
                                fieldWithPath("imageIds")
                                        .attributes(key("constraint").value("????????? ID X. ????????? ???????????? ?????? ID X."))
                                        .description("????????? ID ?????????"),
                                fieldWithPath("content").description("????????? ??????")
                                        .attributes(key("constraint").value("?????? 2000."))
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("????????? ????????? ID")
                        )
                ));
    }

    @Test
    @WithMockUser
    @DisplayName("????????? ?????? API - ???????????? ?????? ????????? ID")
    void post_InvalidImageIds() throws Exception {
        // given
        PostCreate postCreate = PostCreate.builder()
                .restaurantId(1L)
                .hashtags(Set.of(Hashtag.STRONG_TASTE, Hashtag.DATE))
                .imageIds(List.of(2L, 3L, 3L))
                .content("test-content")
                .build();
        given(accountService.getMemberId(anyString())).willReturn(10L);
        given(imageService.validateImageIds(anyList(), eq(10L))).willReturn(false);

        // expected
        mockMvc.perform(post("/v1/post").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .content(objectMapper.writeValueAsString(postCreate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.INVALID_IMAGE_ID.name()));
    }

    @Test
    @WithMockUser
    @DisplayName("????????? ?????? - DOCS")
    void patch_Docs() throws Exception {
        // given
        PostEdit postEdit = PostEdit.builder()
                .restaurantId(1L)
                .hashtags(Set.of(Hashtag.STRONG_TASTE, Hashtag.EATING_ALON))
                .content("edited-content")
                .build();

        given(accountService.getMemberId(anyString())).willReturn(10L);
        given(postService.isNotOwnedByMember(2L, 10L)).willReturn(false);
        given(postService.edit(any(PostEdit.class), eq(2L))).willReturn(new UpdateResult<>(2L));

        // expected
        mockMvc.perform(patch("/v1/posts/{postId}", 2L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .content(objectMapper.writeValueAsString(postEdit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-post-patch",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("????????? ID")
                        ),
                        requestFields(
                                fieldWithPath("restaurantId").type(Long.class.getSimpleName())
                                        .description("????????? ????????? ID"),
                                fieldWithPath("hashtags").description("????????? ???????????? ?????????"),
                                fieldWithPath("content")
                                        .attributes(key("constraint").value("?????? 2000."))
                                        .description("????????? ??????")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("????????? ????????? ID")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("????????? ?????? API - ???????????? ?????? ?????????")
    void patch_NotOwned() throws Exception {
        // given
        PostEdit postEdit = PostEdit.builder()
                .restaurantId(1L)
                .hashtags(Set.of(Hashtag.STRONG_TASTE, Hashtag.EATING_ALON))
                .content("edited-content")
                .build();

        given(accountService.getMemberId(anyString())).willReturn(10L);
        given(postService.isNotOwnedByMember(2L, 10L)).willReturn(true);

        // expected
        mockMvc.perform(patch("/v1/posts/{postId}", 2L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .content(objectMapper.writeValueAsString(postEdit)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.ENTITY_ACCESS_DENIED.name()))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("????????? ?????? API - DOCS")
    void delete_Docs() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(10L);
        given(postService.isNotOwnedByMember(1L, 10L)).willReturn(false);
        given(postService.delete(1L)).willReturn(new DeleteResult<>(1L));

        // expected
        mockMvc.perform(delete("/v1/posts/{postId}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-post-delete",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("????????? ID")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("????????? ????????? ID")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("????????? ?????? API - ???????????? ?????? ?????????")
    void delete_NotOwned() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(10L);
        given(postService.isNotOwnedByMember(1L, 10L)).willReturn(true);

        // expected
        mockMvc.perform(delete("/v1/posts/{postId}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.ENTITY_ACCESS_DENIED.name()))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("????????? ????????? ?????? - DOCS")
    void getList_Docs() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(10L);

        PostDto postDto = PostDto.builder()
                .id(1L)
                .imageUrls(List.of("image-url-1", "image-url-2"))
                .content("post-content")
                .createdAt(LocalDateTime.of(2022, 10, 10, 19, 7))
                .commentCount(10)
                .likeCount(20)
                .memberId(2L)
                .memberNickname("nickname")
                .memberImageUrl("member-image-url")
                .restaurantId(3L)
                .restaurantName("restaurant-name")
                .build();
        given(postService.getList(any(Pageable.class), any(PostSearch.class)))
                .willReturn(new SliceImpl<>(List.of(postDto), PageRequest.of(0, 10), false));

        PostLiked postLiked = PostLiked.builder()
                .postId(1L)
                .liked(true)
                .build();
        given(likeService.areLiked(10L, List.of(1L))).willReturn(List.of(postLiked));

        PostBookmarked postBookmarked = PostBookmarked.builder()
                .postId(1L)
                .bookmarked(false)
                .build();
        given(bookmarkService.areBookmarked(10L, List.of(1L))).willReturn(List.of(postBookmarked));

        // expected
        mockMvc.perform(get("/v1/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("hashtags", Hashtag.EATING_ALON.name(), Hashtag.COST_EFFECTIVENESS.name())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].liked").value(true))
                .andExpect(jsonPath("$.data.content[0].bookmarked").value(false))
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-post-get-list",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        requestParameters(
                                parameterWithName("hashtags")
                                        .description("???????????? hashtag ?????????. " +
                                                "???????????? ??????????????? ?????? ???????????? ?????????. null ?????? ??????.").optional()
                        ),
                        responseFields(
                                beneathPath("data.content[]").withSubsectionId("content"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("????????? ID"),
                                fieldWithPath("imageUrls").description("????????? ????????? URL ?????????"),
                                fieldWithPath("content").description("????????? ??????"),
                                fieldWithPath("createdAt").description("????????? ?????? ??????"),
                                fieldWithPath("commentCount").type(Integer.class.getSimpleName())
                                        .description("????????? ?????? ??????"),
                                fieldWithPath("likeCount").type(Integer.class.getSimpleName())
                                        .description("????????? ????????? ??????"),
                                fieldWithPath("liked").description("????????? ??????"),
                                fieldWithPath("bookmarked").description("????????? ??????"),
                                fieldWithPath("member.id").type(Long.class.getSimpleName())
                                        .description("????????? ????????? ?????? ID"),
                                fieldWithPath("member.nickname").description("????????? ????????? ?????? ?????????"),
                                fieldWithPath("member.imageUrl")
                                        .description("????????? ????????? ?????? ????????? ????????? URL. ?????? ?????? null").optional(),
                                fieldWithPath("restaurant.id").type(Long.class.getSimpleName())
                                        .description("????????? ????????? ID"),
                                fieldWithPath("restaurant.name").description("????????? ????????? ??????")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("?????? ????????? ????????? ?????? - DOCS")
    void getListOfMember_Docs() throws Exception {
        // given
        long memberId = 10L;
        long myMemberId = 11L;
        given(accountService.getMemberId(anyString())).willReturn(myMemberId);

        MemberPostDto memberPostDto = MemberPostDto.builder()
                .id(1L)
                .imageUrls(List.of("image-url-1", "image-url-2"))
                .content("post-content")
                .createdAt(LocalDateTime.of(2022, 10, 10, 19, 7))
                .commentCount(10)
                .likeCount(20)
                .restaurantId(3L)
                .restaurantName("restaurant-name")
                .build();
        given(postService.getListOfMember(any(Pageable.class), eq(memberId)))
                .willReturn(new SliceImpl<>(List.of(memberPostDto)));

        PostLiked postLiked = PostLiked.builder()
                .postId(1L)
                .liked(true)
                .build();
        given(likeService.areLiked(myMemberId, List.of(1L))).willReturn(List.of(postLiked));

        PostBookmarked postBookmarked = PostBookmarked.builder()
                .postId(1L)
                .bookmarked(false)
                .build();
        given(bookmarkService.areBookmarked(myMemberId, List.of(1L))).willReturn(List.of(postBookmarked));

        // expected
        mockMvc.perform(get("/v1/members/{memberId}/posts", 10L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].liked").value(true))
                .andExpect(jsonPath("$.data.content[0].bookmarked").value(false))
                .andDo(document("v1-post-get-list-of-member",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        pathParameters(
                                parameterWithName("memberId").description("?????? ID")
                        ),
                        requestParameters(
                                parameterWithName("sort").optional()
                                        .description("?????? ??????. id,desc: ?????????. likeCount,desc: ????????????")
                        ),
                        responseFields(
                                beneathPath("data.content[]").withSubsectionId("content"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("????????? ID"),
                                fieldWithPath("imageUrls").description("????????? ????????? URL ?????????"),
                                fieldWithPath("content").description("????????? ??????"),
                                fieldWithPath("createdAt").description("????????? ?????? ??????"),
                                fieldWithPath("commentCount").type(Integer.class.getSimpleName())
                                        .description("????????? ?????? ??????"),
                                fieldWithPath("likeCount").type(Integer.class.getSimpleName())
                                        .description("????????? ????????? ??????"),
                                fieldWithPath("liked").description("????????? ??????"),
                                fieldWithPath("bookmarked").description("????????? ??????"),
                                fieldWithPath("restaurant.id").type(Long.class.getSimpleName())
                                        .description("????????? ????????? ID"),
                                fieldWithPath("restaurant.name").description("????????? ????????? ??????")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("????????? ????????? ????????? ?????? - DOCS")
    void getListOfRestaurant() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(10L);

        RestaurantPostDto restaurantPostDto = RestaurantPostDto.builder()
                .id(1L)
                .imageUrls(List.of("image-url-1", "image-url-2"))
                .content("post-content")
                .createdAt(LocalDateTime.of(2022, 10, 10, 19, 7))
                .memberId(2L)
                .memberNickname("nickname")
                .memberImageUrl("member-image-url")
                .build();
        given(postService.getListOfRestaurant(any(Pageable.class), eq(3L)))
                .willReturn(new SliceImpl<>(List.of(restaurantPostDto)));

        PostLiked postLiked = PostLiked.builder()
                .postId(1L)
                .liked(true)
                .build();
        given(likeService.areLiked(10L, List.of(1L))).willReturn(List.of(postLiked));

        PostBookmarked postBookmarked = PostBookmarked.builder()
                .postId(1L)
                .bookmarked(false)
                .build();
        given(bookmarkService.areBookmarked(10L, List.of(1L))).willReturn(List.of(postBookmarked));

        // expected
        mockMvc.perform(get("/v1/restaurants/{restaurantId}/posts", 3L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-post-get-list-of-restaurant",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        pathParameters(
                                parameterWithName("restaurantId").description("????????? ID")
                        ),
                        responseFields(
                                beneathPath("data.content[]").withSubsectionId("content"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("????????? ID"),
                                fieldWithPath("imageUrls").description("????????? ????????? URL ?????????"),
                                fieldWithPath("content").description("????????? ??????"),
                                fieldWithPath("createdAt").description("????????? ?????? ??????"),
                                fieldWithPath("liked").description("????????? ??????"),
                                fieldWithPath("bookmarked").description("????????? ??????"),
                                fieldWithPath("member.id").type(Long.class.getSimpleName())
                                        .description("????????? ????????? ?????? ID"),
                                fieldWithPath("member.nickname").description("????????? ????????? ?????? ?????????"),
                                fieldWithPath("member.imageUrl").optional()
                                        .description("????????? ????????? ?????? ????????? ????????? URL. ?????? ?????? null")
                        )
                ))
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("????????? ?????? ?????? - DOCS")
    void get_Docs() throws Exception {
        // given
        given(accountService.getMemberId(anyString())).willReturn(1L);

        PostDetailDto postDetailDto = PostDetailDto.builder()
                .id(2L)
                .imageUrls(List.of("image-url-1", "image-url-1"))
                .content("content")
                .hashtags(List.of(Hashtag.STRONG_TASTE, Hashtag.CLEANLINESS))
                .createdAt(LocalDateTime.of(2022, 10, 31, 17, 53))
                .commentCount(10)
                .likeCount(20)
                .memberId(3L)
                .memberNickname("nickname")
                .memberImageUrl("member-image-url")
                .restaurantId(4L)
                .restaurantName("????????????")
                .restaurantAddressName("?????????")
                .restaurantAddressX(1.0)
                .restaurantAddressY(2.0)
                .build();
        given(postService.get(2L)).willReturn(postDetailDto);

        given(bookmarkService.isBookmarked(1L, 2L)).willReturn(false);
        given(likeService.isLiked(1L, 2L)).willReturn(true);

        // expected
        mockMvc.perform(get("/v1/posts/{postId}", 2L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {ACCESS_TOKEN}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(document("v1-post-get",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("?????? ??????")
                        ),
                        pathParameters(
                                parameterWithName("postId").description("????????? ID")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").type(Long.class.getSimpleName()).description("????????? ID"),
                                fieldWithPath("imageUrls").description("????????? ????????? URL ?????????"),
                                fieldWithPath("content").description("????????? ??????"),
                                fieldWithPath("hashtags").description("????????? ???????????? ?????????"),
                                fieldWithPath("createdAt").description("????????? ?????? ??????"),
                                fieldWithPath("commentCount").type(Integer.class.getSimpleName())
                                        .description("????????? ?????? ??????"),
                                fieldWithPath("likeCount").type(Integer.class.getSimpleName())
                                        .description("????????? ????????? ??????"),
                                fieldWithPath("liked").description("????????? ??????"),
                                fieldWithPath("bookmarked").description("????????? ??????"),
                                fieldWithPath("member.id").type(Long.class.getSimpleName())
                                        .description("????????? ????????? ?????? ID"),
                                fieldWithPath("member.nickname").description("????????? ????????? ?????? ?????????"),
                                fieldWithPath("member.imageUrl")
                                        .description("????????? ????????? ?????? ????????? ????????? URL. ?????? ?????? null").optional(),
                                fieldWithPath("restaurant.id").type(Long.class.getSimpleName())
                                        .description("????????? ????????? ID"),
                                fieldWithPath("restaurant.name").description("????????? ????????? ??????"),
                                fieldWithPath("restaurant.address.name").description("????????? ????????? ??????"),
                                fieldWithPath("restaurant.address.x").type(Double.class.getSimpleName())
                                        .description("????????? ????????? X ??????"),
                                fieldWithPath("restaurant.address.y").type(Double.class.getSimpleName())
                                        .description("????????? ????????? Y ??????")
                        )
                ))
        ;
    }

}