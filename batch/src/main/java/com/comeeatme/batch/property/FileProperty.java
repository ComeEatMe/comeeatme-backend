package com.comeeatme.batch.property;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Getter
@Slf4j
public class FileProperty implements InitializingBean {

    private final File rootDir;
    private final File addressCodeDir;

    public FileProperty() {
        this.rootDir = new File(System.getProperty("user.dir"), "batch-data");
        this.addressCodeDir = new File(this.rootDir, "address-code");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        mkDirsIfNotExists(rootDir);
        mkDirsIfNotExists(addressCodeDir);
    }

    private void mkDirsIfNotExists(File dir) {
        if (!dir.exists()) {
            log.info("파일 저장을 위한 디렉토리가 존재하지 않습니다.");
            if (!dir.mkdirs()) {
                throw new IllegalStateException("파일 저장을 위한 디렉토리 생성에 실패했습니다.");
            }
        } else {
            if (!dir.isDirectory()) {
                throw new IllegalArgumentException("파일 저장을 위한 경로가 디렉토리가 아닙니다.");
            }
        }
    }
}
