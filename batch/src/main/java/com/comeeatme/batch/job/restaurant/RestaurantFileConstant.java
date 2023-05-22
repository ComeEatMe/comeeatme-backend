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
}
