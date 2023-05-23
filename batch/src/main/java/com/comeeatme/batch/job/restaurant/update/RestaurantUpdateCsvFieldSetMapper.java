package com.comeeatme.batch.job.restaurant.update;

import com.comeeatme.batch.job.restaurant.LocalDataRestaurantDto;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class RestaurantUpdateCsvFieldSetMapper implements FieldSetMapper<LocalDataRestaurantDto> {

    @Override
    public LocalDataRestaurantDto mapFieldSet(FieldSet fieldSet) throws BindException {
        return LocalDataRestaurantDto.builder()
                .rowNum(fieldSet.readString("번호"))
                .opnSfTeamCode(fieldSet.readString("개방자치단체코드"))
                .mgtNo(fieldSet.readString("관리번호"))
                .opnSvcId(fieldSet.readString("개방서비스ID"))
                .updateGbn(fieldSet.readString("데이터갱신구분"))
                .updateDt(fieldSet.readString("데이터갱신일자"))
                .opnSvcNm(fieldSet.readString("개방서비스명"))
                .bplcNm(fieldSet.readString("사업장명"))
                .sitePostNo(fieldSet.readString("지번우편번호"))
                .siteWhlAddr(fieldSet.readString("지번주소"))
                .rdnPostNo(fieldSet.readString("도로명우편번호"))
                .rdnWhlAddr(fieldSet.readString("도로명주소"))
                .siteArea(fieldSet.readString("소재지면적"))
                .apvPermYmd(fieldSet.readString("인허가일자"))
                .apvCancelYmd(fieldSet.readString("인허가취소일자"))
                .dcbYmd(fieldSet.readString("폐업일자"))
                .dtlStateGbn(fieldSet.readString("상세영업상태코드"))
                .dtlStateNm(fieldSet.readString("상세영업상태명"))
                .x(fieldSet.readString("좌표정보(X)"))
                .y(fieldSet.readString("좌표정보(Y)"))
                .lastModTs(fieldSet.readString("최종수정일자"))
                .uptaeNm(fieldSet.readString("업태구분명"))
                .siteTel(fieldSet.readString("전화번호"))
                .build();
    }
}
