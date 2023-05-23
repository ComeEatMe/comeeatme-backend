package com.comeeatme.batch.restaurant;

import com.comeeatme.domain.restaurant.LocalData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;

@Slf4j
public class RestaurantSkipListener implements SkipListener<LocalDataRestaurantDto, LocalData> {

    @Override
    public void onSkipInRead(Throwable t) {
        log.error("", t);
    }

    @Override
    public void onSkipInWrite(LocalData item, Throwable t) {
        log.error("itemId={}, ", item.getId(), t);
    }

    @Override
    public void onSkipInProcess(LocalDataRestaurantDto item, Throwable t) {
        log.error("item={}, ", item, t);
    }
}
