package com.comeeatme.domain.restaurant.kakao.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KakaoPageTest {

    @Test
    void create() throws Exception {
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        String pageJson = "{\n" +
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

        // when
        KakaoPage page = objectMapper.readValue(pageJson, KakaoPage.class);

        // then
        assertThat(page.getDocuments()).isNotNull();
        assertThat(page.getMeta()).isNotNull();
    }
}