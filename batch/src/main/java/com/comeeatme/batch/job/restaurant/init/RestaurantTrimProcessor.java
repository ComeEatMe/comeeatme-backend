package com.comeeatme.batch.job.restaurant.init;

import com.comeeatme.batch.job.restaurant.LocalDataRestaurantDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class RestaurantTrimProcessor implements ItemProcessor<LocalDataRestaurantDto, LocalDataRestaurantDto> {

    @Override
    public LocalDataRestaurantDto process(LocalDataRestaurantDto item) throws Exception {
        item.setRdnWhlAddr(trimAddr(item.getRdnWhlAddr()));
        item.setSiteWhlAddr(trimAddr(item.getSiteWhlAddr()));
        item.setSiteTel(trimSiteTel(item.getSiteTel()));
        return item;
    }

    private String trimAddr(String addr) {
        if (StringUtils.hasText(addr)) {
            addr = addr.replaceAll("\\s+", " ");
        }
        return addr;
    }

    private String trimSiteTel(String siteTel) {
        if (StringUtils.hasText(siteTel)) {
            siteTel = siteTel
                    .replace("-", "")
                    .replaceAll("\\s+", "");
        }
        return siteTel;
    }

}
