package com.comeeatme.batch.job.restaurant.init;

import com.comeeatme.batch.job.restaurant.AddressCodeFinder;
import com.comeeatme.batch.job.restaurant.LocalDataRestaurantDto;
import com.comeeatme.domain.address.Address;
import com.comeeatme.domain.address.AddressCode;
import com.comeeatme.domain.restaurant.LocalData;
import com.comeeatme.domain.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
@StepScope
@RequiredArgsConstructor
public class LocalDataRestaurantEntityBuildProcessor implements ItemProcessor<LocalDataRestaurantDto, LocalData> {

    private final DateTimeFormatter updateDtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AddressCodeFinder addressCodeFinder;

    @Override
    public LocalData process(LocalDataRestaurantDto item) throws Exception {
        AddressCode addressCode = addressCodeFinder.findByFullAddress(item.getSiteWhlAddr());
        Address address = Address.builder()
                .name(item.getSiteWhlAddr())
                .roadName(item.getRdnWhlAddr())
                .addressCode(addressCode)
                .build();
        Restaurant restaurant = Restaurant.builder()
                .name(item.getBplcNm())
                .phone(item.getSiteTel())
                .address(address)
                .build();
        LocalData localData = LocalData.builder()
                .managementNum(item.getMgtNo())
                .restaurant(restaurant)
                .serviceId(item.getOpnSvcId())
                .name(item.getOpnSvcNm())
                .category(item.getUptaeNm())
                .permissionDate(item.getApvPermYmd())
                .closedDate(Optional.ofNullable(item.getDcbYmd()).orElse(""))
                .updateAt(LocalDateTime.parse(item.getUpdateDt(), updateDtFormatter))
                .build();
        if ("폐업".equals(item.getDtlStateNm())) {
            restaurant.delete();
            localData.delete();
            localData.setClosedDate(item.getDcbYmd());
        }
        return localData;
    }

}
