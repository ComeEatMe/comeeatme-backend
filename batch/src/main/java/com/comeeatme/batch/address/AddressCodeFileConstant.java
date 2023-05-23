package com.comeeatme.batch.address;

import com.comeeatme.batch.common.AbstractFileConstant;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Getter
public class AddressCodeFileConstant extends AbstractFileConstant {

    private final File dir;

    private final String txtName = "법정동코드 전체자료.txt";

    private final String zipName = "address_code.zip";

    public AddressCodeFileConstant() {
        this.dir = new File(rootDir, "address-code");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        mkDirsIfNotExists(dir);
    }
}
