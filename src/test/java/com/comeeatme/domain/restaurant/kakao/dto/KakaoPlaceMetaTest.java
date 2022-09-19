package com.comeeatme.domain.restaurant.kakao.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KakaoPlaceMetaTest {

    @Test
    void create() throws Exception {
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        String metaJson = "{\n" +
                "    \"is_end\": true,\n" +
                "    \"pageable_count\": 1,\n" +
                "    \"same_name\": {\n" +
                "      \"keyword\": \"모노끼\",\n" +
                "      \"region\": [],\n" +
                "      \"selected_region\": \"\"\n" +
                "    },\n" +
                "    \"total_count\": 1\n" +
                "  }";

        // when
        KakaoPlaceMeta meta = objectMapper.readValue(metaJson, KakaoPlaceMeta.class);

        // then
        assertThat(meta.getIsEnd()).isTrue();
        assertThat(meta.getPageableCount()).isEqualTo(1);
        assertThat(meta.getTotalCount()).isEqualTo(1);
        assertThat(meta.getSameName().getKeyword()).isEqualTo("모노끼");
        assertThat(meta.getSameName().getRegion()).isEmpty();
        assertThat(meta.getSameName().getSelectedRegion()).isEmpty();
    }
}