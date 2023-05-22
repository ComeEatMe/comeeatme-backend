package com.comeeatme.batch.job.restaurant.init;

import com.comeeatme.batch.job.restaurant.LocalDataRestaurantDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

import java.util.Map;

@RequiredArgsConstructor
public class RestaurantOldAddressProcessor implements ItemProcessor<LocalDataRestaurantDto, LocalDataRestaurantDto> {

    private final Map<String, String> addressOldToChanged = Map.ofEntries(
            Map.entry("강원도 양구군 남면", "강원도 양구군 국토정중앙면"),
            Map.entry("강원도 영월군 중동면", "강원도 영월군 산솔면"),
            Map.entry("강원도 홍천군 동면", "강원도 홍천군 영귀미면"),
            Map.entry("경기도 광주시 오포읍 고산리", "경기도 광주시 고산동"),
            Map.entry("경기도 광주시 오포읍 능평리", "경기도 광주시 능평동"),
            Map.entry("경기도 광주시 오포읍 매산리", "경기도 광주시 매산동"),
            Map.entry("경기도 광주시 오포읍 문형리", "경기도 광주시 문형동"),
            Map.entry("경기도 광주시 오포읍 신현리", "경기도 광주시 신현동"),
            Map.entry("경기도 광주시 오포읍 양벌리", "경기도 광주시 양벌동"),
            Map.entry("경기도 광주시 오포읍 추자리", "경기도 광주시 추자동"),
            Map.entry("경기도 남양주시 퇴계원면", "경기도 남양주시 퇴계원읍"),
            Map.entry("경기도 부천시 원미구", "경기도 부천시"),
            Map.entry("경기도 여주시 능서면", "경기도 여주시 세종대왕면"),
            Map.entry("경상북도 경산시 압량면", "경상북도 경산시 압량읍"),
            Map.entry("경상북도 경주시 양북면", "경상북도 경주시 문무대왕면"),
            Map.entry("경상북도 구미시 산동면", "경상북도 구미시 산동읍"),
            Map.entry("경상북도 군위군 고로면", "경상북도 군위군 삼국유사면"),
            Map.entry("경상북도 상주시 사벌면", "경상북도 상주시 사벌국면"),
            Map.entry("경상북도 청송군 부동면", "경상북도 청송군 주왕산면"),
            Map.entry("경상북도 청송군 부동면 이전리", "경상북도 청송군 주왕산면 주산지리"),
            Map.entry("대구광역시 달성군 옥포면", "대구광역시 달성군 옥포읍"),
            Map.entry("대구광역시 달성군 현풍면", "대구광역시 달성군 현풍읍"),
            Map.entry("부산광역시 기장군 일광면", "부산광역시 기장군 일광읍"),
            Map.entry("울산광역시 울주군 삼남면", "울산광역시 울주군 삼남읍"),
            Map.entry("전라남도 담양군 남면", "전라남도 담양군 가사문학면"),
            Map.entry("전라남도 화순군 남면", "전라남도 화순군 사평면"),
            Map.entry("전라남도 화순군 북면", "전라남도 화순군 백아면"),
            Map.entry("충청북도 진천군 덕산면", "충청북도 진천군 덕산읍"),
            Map.entry("경기도 용인시 처인구 남사면", "경기도 용인시 처인구 남사읍")
    );

    private final Map<String, String> roadAddressOldToChanged = Map.ofEntries(
            Map.entry("강원도 양구군 남면", "강원도 양구군 국토정중앙면"),
            Map.entry("강원도 영월군 중동면", "강원도 영월군 산솔면"),
            Map.entry("강원도 홍천군 동면", "강원도 홍천군 영귀미면"),
            Map.entry("경기도 광주시 오포읍", "경기도 광주시"),
            Map.entry("경기도 남양주시 퇴계원면", "경기도 남양주시 퇴계원읍"),
            Map.entry("경기도 부천시 원미구", "경기도 부천시"),
            Map.entry("경기도 여주시 능서면", "경기도 여주시 세종대왕면"),
            Map.entry("경상북도 경산시 압량면", "경상북도 경산시 압량읍"),
            Map.entry("경상북도 경주시 양북면", "경상북도 경주시 문무대왕면"),
            Map.entry("경상북도 구미시 산동면", "경상북도 구미시 산동읍"),
            Map.entry("경상북도 군위군 고로면", "경상북도 군위군 삼국유사면"),
            Map.entry("경상북도 상주시 사벌면", "경상북도 상주시 사벌국면"),
            Map.entry("경상북도 청송군 부동면", "경상북도 청송군 주왕산면"),
            Map.entry("경상북도 청송군 부동면 이전리", "경상북도 청송군 주왕산면 주산지리"),
            Map.entry("대구광역시 달성군 옥포면", "대구광역시 달성군 옥포읍"),
            Map.entry("대구광역시 달성군 현풍면", "대구광역시 달성군 현풍읍"),
            Map.entry("부산광역시 기장군 일광면", "부산광역시 기장군 일광읍"),
            Map.entry("울산광역시 울주군 삼남면", "울산광역시 울주군 삼남읍"),
            Map.entry("전라남도 담양군 남면", "전라남도 담양군 가사문학면"),
            Map.entry("전라남도 화순군 남면", "전라남도 화순군 사평면"),
            Map.entry("전라남도 화순군 북면", "전라남도 화순군 백아면"),
            Map.entry("충청북도 진천군 덕산면", "충청북도 진천군 덕산읍")
    );

    @Override
    public LocalDataRestaurantDto process(LocalDataRestaurantDto item) throws Exception {
        for (Map.Entry<String, String> oldChanged : addressOldToChanged.entrySet()) {
            String oldAddr = oldChanged.getKey();
            String changedAddr = oldChanged.getValue();
            if (item.getSiteWhlAddr().startsWith(oldAddr)) {
                item.setSiteWhlAddr(item.getSiteWhlAddr().replace(oldAddr, changedAddr));
            }
        }

        for (Map.Entry<String, String> oldChanged : roadAddressOldToChanged.entrySet()) {
            String oldAddr = oldChanged.getKey();
            String changedAddr = oldChanged.getValue();
            if (item.getRdnWhlAddr().startsWith(oldAddr)) {
                item.setRdnWhlAddr(item.getRdnWhlAddr().replace(oldAddr, changedAddr));
            }
        }
        return item;
    }

}
