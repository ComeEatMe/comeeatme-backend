package com.comeeatme.domain.restaurant.kakao.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KakaoPlaceTest {

    @Test
    void create() throws Exception {
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        String kakaoRestaurantJson = "{\n" +
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
                "    }";

        // when
        KakaoPlace kakaoPlace = objectMapper.readValue(kakaoRestaurantJson, KakaoPlace.class);

        // then
        assertThat(kakaoPlace.getAddressName()).isEqualTo("경기 성남시 분당구 야탑동 353-4");
        assertThat(kakaoPlace.getCategoryGroupCode()).isEqualTo(KakaoCategoryGroupCode.FD6);
        assertThat(kakaoPlace.getCategoryGroupName()).isEqualTo("음식점");
        assertThat(kakaoPlace.getCategoryName()).isEqualTo("음식점 > 일식 > 돈까스,우동");
        assertThat(kakaoPlace.getDistance()).isNull();
        assertThat(kakaoPlace.getId()).isEqualTo(25970354);
        assertThat(kakaoPlace.getPhone()).isEqualTo("031-702-2929");
        assertThat(kakaoPlace.getPlaceName()).isEqualTo("모노끼 야탑점");
        assertThat(kakaoPlace.getPlaceUrl()).isEqualTo("http://place.map.kakao.com/25970354");
        assertThat(kakaoPlace.getRoadAddressName()).isEqualTo("경기 성남시 분당구 야탑로69번길 24-6");
        assertThat(kakaoPlace.getX()).isEqualTo("127.12729076428592");
        assertThat(kakaoPlace.getY()).isEqualTo("37.41160407198509");
    }
}