package com.comeeatme.domain.account;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.account.repository.AccountRepository;
import com.comeeatme.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatNoException;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class AccountTest {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("Account 생성 및 저장")
    void save() {
        assertThatNoException().isThrownBy(() -> accountRepository.save(Account.builder()
                .member(memberRepository.getReferenceById(2L))
                .username("test-username")
                .password(null)
                .build()));
    }

}