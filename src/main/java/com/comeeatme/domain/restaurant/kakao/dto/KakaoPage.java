package com.comeeatme.domain.restaurant.kakao.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;

import java.util.List;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KakaoPage {
    private List<KakaoPlace> documents;
    private KakaoPlaceMeta meta;
}
