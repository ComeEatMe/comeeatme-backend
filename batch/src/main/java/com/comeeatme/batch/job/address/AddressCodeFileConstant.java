package com.comeeatme.batch.job.address;

import com.comeeatme.batch.job.AbstractFileConstant;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Getter
public class AddressCodeFileConstant extends AbstractFileConstant {

    private final File dir;

    private final String zipName = "address_code.zip";

    public AddressCodeFileConstant() {
        this.dir = new File(rootDir, "address-code");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        mkDirsIfNotExists(dir);
    }
}
