package com.comeeatme.domain.address;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.address.repository.AddressCodeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class AddressCodeTest {

    @Autowired
    private AddressCodeRepository addressCodeRepository;

    @Test
    @DisplayName("AddressCode 생성 및 저장")
    void save() {
        assertThatNoException().isThrownBy(() -> addressCodeRepository.saveAllAndFlush(List.of(
                AddressCode.builder()
                        .code("1100000000")
                        .name("서울특별시")
                        .fullName("서울특별시")
                        .depth(1)
                        .terminal(false)
                        .build(),
                AddressCode.builder()
                        .code("1121500000")
                        .name("서울특별시 광진구")
                        .fullName("광진구")
                        .depth(2)
                        .terminal(false)
                        .build(),
                AddressCode.builder()
                        .code("1121510700")
                        .name("서울특별시 광진구 화양동")
                        .fullName("화양동")
                        .depth(3)
                        .terminal(true)
                        .build()
        )));
    }

}