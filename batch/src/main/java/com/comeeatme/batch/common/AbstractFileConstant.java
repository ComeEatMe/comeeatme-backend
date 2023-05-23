package com.comeeatme.batch.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;

@Slf4j
public abstract class AbstractFileConstant implements InitializingBean {

    protected final File rootDir;

    protected AbstractFileConstant() {
        this.rootDir = new File(System.getProperty("user.dir"), "batch-data");
    }

    protected void mkDirsIfNotExists(File dir) {
        if (!dir.exists()) {
            log.info("디렉토리가 존재하지 않습니다. dir = {}", dir);
            if (!dir.mkdirs()) {
                throw new IllegalStateException("디렉토리 생성에 실패했습니다. dir = " + dir);
            }
        } else {
            if (!dir.isDirectory()) {
                throw new IllegalArgumentException("해당 경로는 디렉토리가 아닙니다. dir = " + dir);
            }
        }
    }
}
