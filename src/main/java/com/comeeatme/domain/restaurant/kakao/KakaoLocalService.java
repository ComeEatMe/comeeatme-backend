package com.comeeatme.domain.restaurant.kakao;

import com.comeeatme.domain.restaurant.kakao.dto.CategoryQueryParam;
import com.comeeatme.domain.restaurant.kakao.dto.KakaoPlacePage;
import com.comeeatme.domain.restaurant.kakao.dto.KeywordQueryParam;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

/**
 * 키워드로 장소 검색 URI: https://dapi.kakao.com/v2/local/search/keyword.json
 * ex) https://dapi.kakao.com/v2/local/search/keyword.json?page=1&size=15&sort=accuracy&category_group_code=FD6&query=%EB%AA%A8%EB%85%B8%EB%81%BC
 * 카테고리로 장소 검색 URI: https://dapi.kakao.com/v2/local/search/category.json
 * ex) https://dapi.kakao.com/v2/local/search/category.json?category_group_code=MT1&page=1&size=15&sort=accuracy
 */
@Service
public class KakaoLocalService {

    private static final String AUTH_PREFIX = "KakaoAK";

    private final String keywordQueryPath;

    private final String categoryQueryPath;

    private final WebClient webClient;

    public KakaoLocalService(
            @Value("${kakao.local.base-url}") String baseUrl,
            @Value("${kakao.local.keyword-query-path}") String keywordQueryPath,
            @Value("${kakao.local.category-query-path}") String categoryQueryPath,
            @Value("${spring.security.oauth2.client.registration.kakao.client-id}") String restApiKey) {
        this.keywordQueryPath = keywordQueryPath;
        this.categoryQueryPath = categoryQueryPath;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(httpHeaders ->
                        httpHeaders.add(HttpHeaders.AUTHORIZATION, AUTH_PREFIX + " " + restApiKey))
                .build();
    }

    /**
     * Ref: https://developers.kakao.com/tool/rest-api/open/get/v2-local-search-keyword.%7Bformat%7D
     */
    public KakaoPlacePage queryByKeyword(KeywordQueryParam param) {

        return webClient.get()
                .uri(builder -> builder
                        .path(keywordQueryPath)
                        .queryParam("query", param.getQuery())
                        .queryParam("category_group_code", param.getCategoryGroupCode())
                        .queryParamIfPresent("x", Optional.ofNullable(param.getX()))
                        .queryParamIfPresent("y", Optional.ofNullable(param.getY()))
                        .queryParamIfPresent("rect", Optional.ofNullable(param.getRect()))
                        .queryParamIfPresent("page", Optional.ofNullable(param.getPage()))
                        .queryParamIfPresent("sort", Optional.ofNullable(param.getSortParam()))
                        .build())
                .retrieve()
                .bodyToMono(KakaoPlacePage.class)
                .block();
    }

    /**
     * Ref: https://developers.kakao.com/tool/rest-api/open/get/v2-local-search-category.%7Bformat%7D
     */
    public KakaoPlacePage queryByCategory(CategoryQueryParam param) {

        return webClient.get()
                .uri(builder -> builder
                        .path(categoryQueryPath)
                        .queryParam("category_group_code", param.getCategoryGroupCode())
                        .queryParamIfPresent("x", Optional.ofNullable(param.getX()))
                        .queryParamIfPresent("y", Optional.ofNullable(param.getY()))
                        .queryParamIfPresent("rect", Optional.ofNullable(param.getRect()))
                        .queryParamIfPresent("page", Optional.ofNullable(param.getPage()))
                        .queryParamIfPresent("sort", Optional.ofNullable(param.getSortParam()))
                        .build())
                .retrieve()
                .bodyToMono(KakaoPlacePage.class)
                .block();
    }
}
