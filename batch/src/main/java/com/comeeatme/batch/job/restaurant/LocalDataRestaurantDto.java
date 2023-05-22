package com.comeeatme.batch.job.restaurant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LocalDataRestaurantDto {

    private String rowNum;              // 번호
    private String opnSfTeamCode;       // 개방자치단체코드
    private String mgtNo;               // 관리번호
    private String opnSvcId;            // 개방서비스ID/개방서비스아이디
    private String updateGbn;           // 데이터갱신구분
    private String updateDt;            // 데이터갱신일자
    private String opnSvcNm;            // 개방서비스명
    private String bplcNm;              // 사업장명
    private String sitePostNo;          // 지번우편번호/소재지우편번호
    private String siteWhlAddr;         // 지번주소/소재지전체주소
    private String rdnPostNo;           // 도로명우편번호
    private String rdnWhlAddr;          // 도로명주소/도로명전체주소
    private String siteArea;            // 소재지면적
    private String apvPermYmd;          // 인허가일자
    private String apvCancelYmd;        // 인허가취소일자
    private String dcbYmd;              // 폐업일자
    private String dtlStateGbn;         // 상세영업상태코드
    private String dtlStateNm;          // 상세영업상태명
    private String x;                   // 좌표정보(X)/좌표정보(x)
    private String y;                   // 좌표정보(Y)/좌표정보(y)
    private String lastModTs;           // 최종수정일자/최종수정시점
    private String uptaeNm;             // 업태구분명
    private String siteTel;             // 전화번호/소재지전화

}
