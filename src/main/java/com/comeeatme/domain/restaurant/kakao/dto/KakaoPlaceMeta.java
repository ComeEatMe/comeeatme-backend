package com.comeeatme.domain.restaurant.kakao.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;

import java.util.List;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KakaoPlaceMeta {
    private Boolean isEnd;
    private Integer pageableCount;
    private SameName sameName;
    private Integer totalCount;

    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SameName {
        private String keyword;
        private List<String> region;
        private String selectedRegion;
    }
}
