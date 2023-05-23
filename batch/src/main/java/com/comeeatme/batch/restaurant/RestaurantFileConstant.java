package com.comeeatme.batch.restaurant;

import com.comeeatme.batch.common.AbstractFileConstant;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Getter
public class RestaurantFileConstant extends AbstractFileConstant {

    private final File dir;

    private final File initDir;

    private final File updateDir;

    private final String localDataAuthKey;

    public RestaurantFileConstant(@Value("${open-api.local-data.key}") String localDataAuthKey) {
        this.dir = new File(rootDir, "restaurant");
        this.initDir = new File(dir, "init");
        this.updateDir = new File(dir, "update");
        this.localDataAuthKey = localDataAuthKey;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        mkDirsIfNotExists(dir);
        mkDirsIfNotExists(initDir);
        mkDirsIfNotExists(updateDir);
    }

    public String getInitZipName(String serviceId) {
        return "init-" + serviceId + ".zip";
    }

    public String getInitFileName(String serviceId) {
        if ("07_24_04_P".equals(serviceId)) {
            return "fulldata_07_24_04_P_일반음식점.csv";
        } else if ("07_24_05_P".equals(serviceId)) {
            return "fulldata_07_24_05_P_휴게음식점.csv";
        }

        throw new IllegalArgumentException(serviceId + "는 지원하지 않는 지역데이터 서비스 아이디 입니다.");
    }

    public String getUpdateFileName(String serviceId, String date) {
        return "update-" + date + "-" + serviceId + ".csv";
    }
}
