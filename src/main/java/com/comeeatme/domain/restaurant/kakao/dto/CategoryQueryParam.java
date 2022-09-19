package com.comeeatme.domain.restaurant.kakao.dto;

import lombok.Getter;

import java.util.Optional;

@Getter
public class CategoryQueryParam {
    /**
     * 카테고리 그룹 코드
     * 결과를 카테고리로 필터링을 원하는 경우 사용
     */
    private KakaoCategoryGroupCode categoryGroupCode = KakaoCategoryGroupCode.FD6;

    /**
     * 중심 좌표의 X값 혹은 longitude, 특정 지역을 중심으로 검색하려고 할 경우 radius와 함께 사용 가능
     */
    private Double x;

    /**
     * 중심 좌표의 Y값 혹은 latitude, 특정 지역을 중심으로 검색하려고 할 경우 radius와 함께 사용 가능
     */
    private Double y;

    /**
     * 중심 좌표부터의 반경거리. 특정 지역을 중심으로 검색하려고 할 경우 중심좌표로 쓰일 x,y와 함께 사용.
     * 단위 meter. 0~20000 사이의 값
     */
    private Integer radius;

    /**
     * 사각형 범위내에서 제한 검색을 위한 좌표. 지도 화면 내 검색시 등 제한 검색에서 사용 가능.
     * 좌측 X 좌표, 좌측 Y 좌표, 우측 X 좌표, 우측 Y 좌표 형식.
     */
    private String rect;

    /**
     * 결과 페이지 번호, 1-45 사이, 기본 값 1
     */
    private Integer page;

    /**
     * 한 페이지에 보여질 문서의 개수, 1~15 사이, 기본 값 15
     */
    private Integer size;

    /**
     * 결과 정렬 순서, distance 정렬을 원할 때는 기준 좌표로 쓰일 x, y와 함께 사용.
     * distance 또는 accuracy, 기본 accuracy
     */
    private Sort sort;

    public String getSortParam() {
        return Optional.ofNullable(sort)
                .map(s -> s.name().toLowerCase())
                .orElse(null);
    }

    public enum Sort {
        ACCURACY,
        DISTANCE,
        ;
    }
}
