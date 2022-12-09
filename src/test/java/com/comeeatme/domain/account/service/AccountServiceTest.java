package com.comeeatme.domain.account.service;

import com.comeeatme.domain.account.Account;
import com.comeeatme.domain.account.repository.AccountRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Test
    void getMemberId() {
        // given
        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);

        Account account = mock(Account.class);
        given(account.getUseYn()).willReturn(true);
        given(account.getMember()).willReturn(member);

        given(accountRepository.findByUsername("username")).willReturn(Optional.of(account));

        // when
        Long result = accountService.getMemberId("username");

        // then
        assertThat(result).isEqualTo(1L);
    }

}