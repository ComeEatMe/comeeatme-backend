package com.comeeatme.batch.job.restaurant;

import com.comeeatme.batch.job.AbstractFileConstant;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Getter
public class RestaurantFileConstant extends AbstractFileConstant {

    private final File dir;

    private final File initDir;

    public RestaurantFileConstant() {
        this.dir = new File(rootDir, "restaurant");
        this.initDir = new File(dir, "init");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        mkDirsIfNotExists(dir);
        mkDirsIfNotExists(initDir);
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
}
