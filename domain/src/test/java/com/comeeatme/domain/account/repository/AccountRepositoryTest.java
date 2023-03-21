package com.comeeatme.domain.account.repository;

import com.comeeatme.domain.account.Account;
import com.comeeatme.domain.common.TestJpaConfig;
import com.comeeatme.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestJpaConfig.class)
@DataJpaTest
@Transactional
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    void findByUsernameAndUseYnIsTrue() {
        // given
        List<Account> accounts = accountRepository.saveAll(List.of(
                Account.builder()
                        .username("username-1")
                        .member(memberRepository.getReferenceById(1L))
                        .build(),
                Account.builder()
                        .username("username-1")
                        .member(memberRepository.getReferenceById(1L))
                        .build()
        ));
        accounts.get(0).delete();

        // when
        Account account = accountRepository.findByUsernameAndUseYnIsTrue("username-1").orElseThrow();

        // then
        assertThat(account.getId()).isEqualTo(accounts.get(1).getId());
    }
}