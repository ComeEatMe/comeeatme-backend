package com.comeeatme.batch.service.juso.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class JusoAddressDto {

    private JusoCommonDto common;

    private List<Juso> juso;

    @Data
    @NoArgsConstructor
    public static class Juso {
        private String roadAddr;            // Y	전체 도로명주소
        private String roadAddrPart1;       // Y	도로명주소(참고항목 제외)
        private String roadAddrPart2;       // N	도로명주소 참고항목
        private String jibunAddr;           // Y	지번주소
        private String engAddr;             // Y	도로명주소(영문)
        private String zipNo;               // Y	우편번호
        private String admCd;               // Y	행정구역코드
        private String rnMgtSn;             // Y	도로명코드
        private String bdMgtSn;             // Y	건물관리번호
        private String detBdNmList;         // N	상세건물명
        private String bdNm;                // N	건물명
        private String bdKdcd;              // Y	공동주택여부(1 : 공동주택, 0 : 비공동주택)
        private String siNm;                // Y	시도명
        private String sggNm;               // Y	시군구명
        private String emdNm;               // Y	읍면동명
        private String liNm;                // N	법정리명
        private String rn;                  // Y	도로명
        private String udrtYn;              // Y	지하여부(0 : 지상, 1 : 지하)
        private Integer buldMnnm;           // Y	건물본번
        private Integer buldSlno;           // Y	건물부번
        private String mtYn;                // Y	산여부(0 : 대지, 1 : 산)
        private Integer lnbrMnnm;           // Y	지번본번(번지)
        private Integer lnbrSlno;           // Y	지번부번(호)
        private String emdNo;               // Y	읍면동일련번호
        private String hstryYn;             // Y    변동이력여부(0: 현행 주소정보, 1: 요청변수의 keyword(검색어)가 변동된 주소정보에서 검색된 정보)
        private String relJibun;            // N    관련지번
        private String hemdNm;              // N    관할주민센터
    }

}
