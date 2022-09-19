package com.comeeatme.domain.restaurant.kakao;

import com.comeeatme.domain.restaurant.kakao.dto.*;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class KakaoLocalServiceTest {

    private MockWebServer mockWebServer;

    private KakaoLocalService kakaoLocalService;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        kakaoLocalService = new KakaoLocalService(
                String.format("http://%s:%s", mockWebServer.getHostName(), mockWebServer.getPort()),
                "/v2/local/search/keyword.json",
                "/v2/local/search/category.json",
                "{REST_API_KEY}"
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void queryByKeyword() throws Exception {
        // given
        String resultJson = "{\n" +
                "  \"documents\": [\n" +
                "    {\n" +
                "      \"address_name\": \"경기 성남시 분당구 야탑동 353-4\",\n" +
                "      \"category_group_code\": \"FD6\",\n" +
                "      \"category_group_name\": \"음식점\",\n" +
                "      \"category_name\": \"음식점 > 일식 > 돈까스,우동\",\n" +
                "      \"distance\": \"\",\n" +
                "      \"id\": \"25970354\",\n" +
                "      \"phone\": \"031-702-2929\",\n" +
                "      \"place_name\": \"모노끼 야탑점\",\n" +
                "      \"place_url\": \"http://place.map.kakao.com/25970354\",\n" +
                "      \"road_address_name\": \"경기 성남시 분당구 야탑로69번길 24-6\",\n" +
                "      \"x\": \"127.12729076428592\",\n" +
                "      \"y\": \"37.41160407198509\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"meta\": {\n" +
                "    \"is_end\": true,\n" +
                "    \"pageable_count\": 1,\n" +
                "    \"same_name\": {\n" +
                "      \"keyword\": \"모노끼\",\n" +
                "      \"region\": [],\n" +
                "      \"selected_region\": \"\"\n" +
                "    },\n" +
                "    \"total_count\": 1\n" +
                "  }\n" +
                "}";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(SC_OK)
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .setBody(resultJson)
        );
        KeywordQueryParam mockParam = mock(KeywordQueryParam.class);
        given(mockParam.getQuery()).willReturn("모노끼");
        given(mockParam.getCategoryGroupCode()).willReturn(KakaoCategoryGroupCode.FD6);
        given(mockParam.getX()).willReturn(null);
        given(mockParam.getY()).willReturn(null);
        given(mockParam.getRadius()).willReturn(null);
        given(mockParam.getRect()).willReturn(null);
        given(mockParam.getPage()).willReturn(null);
        given(mockParam.getSize()).willReturn(null);
        given(mockParam.getSortParam()).willReturn(null);

        // when
        kakaoLocalService.queryByKeyword(mockParam);

        // then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo(HttpMethod.GET.name());
        assertThat(recordedRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("KakaoAK " + "{REST_API_KEY}");
        assertThat(recordedRequest.getPath()).startsWith("/v2/local/search/keyword.json");

        HttpUrl requestUrl = recordedRequest.getRequestUrl();
        assertThat(requestUrl).isNotNull();
        assertThat(requestUrl.queryParameter("query")).isEqualTo("모노끼");
        assertThat(requestUrl.queryParameter("category_group_code"))
                .isEqualTo(KakaoCategoryGroupCode.FD6.name());
        assertThat(requestUrl.queryParameter("x")).isNull();
        assertThat(requestUrl.queryParameter("y")).isNull();
        assertThat(requestUrl.queryParameter("radius")).isNull();
        assertThat(requestUrl.queryParameter("rect")).isNull();
        assertThat(requestUrl.queryParameter("page")).isNull();
        assertThat(requestUrl.queryParameter("size")).isNull();
        assertThat(requestUrl.queryParameter("sort")).isNull();
    }
}