package com.comeeatme.domain.address;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.address.repository.AddressCodeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@DataJpaTest
@Import(TestJpaConfig.class)
@Transactional
class AddressCodeTest {

    @Autowired
    private AddressCodeRepository addressCodeRepository;

    @Test
    @DisplayName("AddressCode 생성 및 저장")
    void save() {
        AddressCode addressCode = AddressCode.builder()
                .code("4113510700")
                .siDo("경기도")
                .siGunGu("성남시분당구")
                .eupMyeonDong("야탑동")
                .build();
        assertThat(addressCode.getFullName()).isEqualTo("경기도성남시분당구야탑동");
        assertThatNoException().isThrownBy(() -> addressCodeRepository.saveAndFlush(addressCode));
    }

}