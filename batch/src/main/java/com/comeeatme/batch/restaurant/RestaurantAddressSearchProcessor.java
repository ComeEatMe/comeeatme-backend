package com.comeeatme.batch.restaurant;

import com.comeeatme.batch.restaurant.LocalDataRestaurantDto;
import com.comeeatme.batch.restaurant.exception.NoAddressException;
import com.comeeatme.batch.service.juso.JusoService;
import com.comeeatme.batch.service.juso.dto.JusoAddressDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RestaurantAddressSearchProcessor implements ItemProcessor<LocalDataRestaurantDto, LocalDataRestaurantDto> {

    private final JusoService jusoService;

    @Override
    public LocalDataRestaurantDto process(LocalDataRestaurantDto item) throws Exception {
        if (!StringUtils.hasText(item.getRdnWhlAddr()) && !StringUtils.hasText(item.getSiteWhlAddr())) {
            throw new NoAddressException(item);
        }

        if (!StringUtils.hasText(item.getSiteWhlAddr())) {
            List<String> addressTokens = Arrays.stream(item.getRdnWhlAddr().split("[\\s,]"))
                    .collect(Collectors.toList());
            while (!addressTokens.isEmpty()) {
                String addressKeyword = String.join(" ", addressTokens);
                JusoAddressDto jusoAddressDto = jusoService.searchAddress(addressKeyword);

                if (!CollectionUtils.isEmpty(jusoAddressDto.getJuso())) {
                    JusoAddressDto.Juso juso = jusoAddressDto.getJuso().get(0);
                    item.setSiteWhlAddr(juso.getJibunAddr());
                    return item;
                }
                addressTokens.remove(addressTokens.size() - 1);
            }
            throw new NoAddressException(item);
        }

        return item;
    }
}
