package com.comeeatme.batch.service.juso.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JusoCommonDto {
    private String totalCount;      // Y    총 검색 데이터수
    private Integer currentPage;    // Y    페이지 번호
    private Integer countPerPage;   // Y    페이지당 출력할 결과 Row 수
    private String errorCode;       // Y    에러 코드
    private String errorMessage;    // Y    에러 메시지

}
