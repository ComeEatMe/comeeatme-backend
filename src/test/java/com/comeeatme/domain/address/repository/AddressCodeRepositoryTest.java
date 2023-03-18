package com.comeeatme.domain.address.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.address.AddressCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class AddressCodeRepositoryTest {

    @Autowired
    private AddressCodeRepository addressCodeRepository;

    @Test
    void existsByNameStartingWith_True() {
        // given
        addressCodeRepository.saveAll(List.of(
                AddressCode.builder()
                        .code("4113510700")
                        .name("야탑동")
                        .fullName("경기도 성남시 분당구 야탑동")
                        .depth(3)
                        .terminal(true)
                        .build(),
                AddressCode.builder()
                        .code("1121510700")
                        .name("서울특별시 광진구 화양동")
                        .fullName("화양동")
                        .depth(3)
                        .terminal(true)
                        .build()
        ));

        // expected
        assertThat(addressCodeRepository.existsByNameStartingWith("야탑")).isTrue();
    }

    @Test
    void existsByNameStartingWith_False() {
        // given
        addressCodeRepository.saveAll(List.of(
                AddressCode.builder()
                        .code("4113510700")
                        .name("야탑동")
                        .fullName("경기도 성남시 분당구 야탑동")
                        .depth(3)
                        .terminal(true)
                        .build(),
                AddressCode.builder()
                        .code("1121510700")
                        .name("서울특별시 광진구 화양동")
                        .fullName("화양동")
                        .depth(3)
                        .terminal(true)
                        .build()
        ));

        // expected
        assertThat(addressCodeRepository.existsByNameStartingWith("정자")).isFalse();
    }

    @Test
    void findByNameStartingWith() {
        // given
        addressCodeRepository.save(
                AddressCode.builder()
                        .code("4113510700")
                        .name("야탑동")
                        .fullName("경기도 성남시 분당구 야탑동")
                        .depth(3)
                        .terminal(true)
                        .build()
        );

        // when
        Optional<AddressCode> result = addressCodeRepository.findByNameStartingWith("야탑");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("4113510700");
    }

    @Test
    void findByNameStartingWith_Empty() {
        // given
        addressCodeRepository.save(
                AddressCode.builder()
                        .code("4113510700")
                        .name("야탑동")
                        .fullName("경기도 성남시 분당구 야탑동")
                        .depth(3)
                        .terminal(true)
                        .build()
        );

        // when
        Optional<AddressCode> result = addressCodeRepository.findByNameStartingWith("화양");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findAllByNameStartingWith() {
        // given
        addressCodeRepository.saveAll(List.of(
                AddressCode.builder()
                        .code("4113510700")
                        .name("야탑동")
                        .fullName("경기도 성남시 분당구 야탑동")
                        .depth(3)
                        .terminal(true)
                        .build(),
                AddressCode.builder()
                        .code("1121510700")
                        .name("서울특별시 광진구 화양동")
                        .fullName("화양동")
                        .depth(3)
                        .terminal(true)
                        .build()
        ));

        // when
        List<AddressCode> result = addressCodeRepository.findAllByNameStartingWith("야탑");

        // expected
        assertThat(result)
                .hasSize(1)
                .extracting("name").containsOnly("야탑동");
    }

    @Test
    void findAllByParentCodeIn() {
        // given
        List<AddressCode> parents = addressCodeRepository.saveAll(List.of(
                AddressCode.builder()
                        .code("4113500000")
                        .name("성남시 분당구")
                        .fullName("경기도 성남시 분당구")
                        .depth(2)
                        .terminal(true)
                        .build(),
                AddressCode.builder()
                        .code("1121500000")
                        .name("광진구")
                        .fullName("서울특별시 광진구")
                        .depth(2)
                        .terminal(true)
                        .build()
        ));
        addressCodeRepository.saveAll(List.of(
                AddressCode.builder()
                        .code("4113510700")
                        .parentCode(parents.get(0))
                        .name("야탑동")
                        .fullName("경기도 성남시 분당구 야탑동")
                        .depth(3)
                        .terminal(true)
                        .build(),
                AddressCode.builder()
                        .code("1121510700")
                        .parentCode(parents.get(1))
                        .name("서울특별시 광진구 화양동")
                        .fullName("화양동")
                        .depth(3)
                        .terminal(true)
                        .build()
        ));

        // when
        List<AddressCode> result = addressCodeRepository.findAllByParentCodeIn(List.of(parents.get(0)));

        // then
        assertThat(result)
                .hasSize(1)
                .extracting("name").containsOnly("야탑동");
    }

    @Test
    void findAllByParentCode() {
        // given
        List<AddressCode> parents = addressCodeRepository.saveAll(List.of(
                AddressCode.builder()
                        .code("4113500000")
                        .name("성남시 분당구")
                        .fullName("경기도 성남시 분당구")
                        .depth(2)
                        .terminal(true)
                        .build(),
                AddressCode.builder()
                        .code("1121500000")
                        .name("광진구")
                        .fullName("서울특별시 광진구")
                        .depth(2)
                        .terminal(true)
                        .build()
        ));
        List<AddressCode> addressCodes = addressCodeRepository.saveAll(List.of(
                AddressCode.builder()
                        .code("4113510700")
                        .parentCode(parents.get(0))
                        .name("야탑동")
                        .fullName("경기도 성남시 분당구 야탑동")
                        .depth(3)
                        .terminal(true)
                        .build(),
                AddressCode.builder()
                        .code("4113510300")
                        .parentCode(parents.get(0))
                        .name("정자동")
                        .fullName("경기도 성남시 분당구 정자동")
                        .depth(3)
                        .terminal(true)
                        .build(),
                AddressCode.builder()
                        .code("4113510301")
                        .parentCode(parents.get(0))
                        .name("삭제동")
                        .fullName("경기도 성남시 분당구 삭제동")
                        .depth(3)
                        .terminal(true)
                        .build(),
                AddressCode.builder()
                        .code("1121510700")
                        .parentCode(parents.get(1))
                        .name("서울특별시 광진구 화양동")
                        .fullName("화양동")
                        .depth(3)
                        .terminal(true)
                        .build()
        ));
        addressCodes.get(2).delete();

        // when
        AddressCode parentCode = parents.get(0);
        List<AddressCode> result = addressCodeRepository.findAllByParentCodeAndUseYnIsTrue(parentCode);

        // then
        assertThat(result)
                .hasSize(2)
                .extracting("code").containsOnly("4113510700", "4113510300");
    }

}