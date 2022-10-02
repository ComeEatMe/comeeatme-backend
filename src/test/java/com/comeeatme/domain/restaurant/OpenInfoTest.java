package com.comeeatme.domain.restaurant;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.restaurant.repository.OpenInfoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatNoException;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class OpenInfoTest {

    @Autowired
    private OpenInfoRepository openInfoRepository;

    @Test
    @DisplayName("OpenInfo 생성 및 저장")
    void save() {
        assertThatNoException().isThrownBy(() -> openInfoRepository.saveAndFlush(OpenInfo.builder()
                .managementNum("3810000-101-2002-00170")
                .serviceId("07_24_04_P")
                .name("일반음식점")
                .category("경양식")
                .permissionDate(LocalDate.of(2012, 12, 13))
                .lastModifiedAt(LocalDateTime.of(2022, 8, 31, 13, 13, 1))
                .build()
        ));
    }

}