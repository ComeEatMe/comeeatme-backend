package com.comeeatme.api.account;

import com.comeeatme.domain.account.Account;
import com.comeeatme.domain.account.repository.AccountRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MemberRepository memberRepository;

    @Test
    void getMemberId() {
        // given
        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);

        Account account = mock(Account.class);
        given(account.getMember()).willReturn(member);

        given(accountRepository.findByUsernameAndUseYnIsTrue("username")).willReturn(Optional.of(account));

        // when
        Long result = accountService.getMemberId("username");

        // then
        assertThat(result).isEqualTo(1L);
    }

    @Test
    void login() {
        // given
        Account account = mock(Account.class);
        given(account.getUseYn()).willReturn(true);
        given(accountRepository.findByUsernameAndUseYnIsTrue("username")).willReturn(Optional.of(account));

        // when
        Account result = accountService.login("username", "refresh-token", LocalDateTime.of(2022, 12, 9, 17, 23));

        // then
        assertThat(result).isEqualTo(account);
        then(account).should().renewRefreshToken("refresh-token", LocalDateTime.of(2022, 12, 9, 17, 23));
    }

    @Test
    void login_AccountNotExists() {
        // given
        given(accountRepository.findByUsernameAndUseYnIsTrue("username")).willReturn(Optional.empty());

        Account account = mock(Account.class);
        given(account.getUseYn()).willReturn(true);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        given(accountRepository.save(accountCaptor.capture())).willReturn(account);

        // when
        Account result = accountService.login("username", "refresh-token", LocalDateTime.of(2022, 12, 9, 17, 23));

        // then
        assertThat(result).isEqualTo(account);
        then(account).should().renewRefreshToken("refresh-token", LocalDateTime.of(2022, 12, 9, 17, 23));
    }

    @Test
    void logout() {
        // given
        Account account = mock(Account.class);
        given(accountRepository.findByUsernameAndUseYnIsTrue("username")).willReturn(Optional.of(account));

        // when
        accountService.logout("username");

        // then
        then(account).should().refreshTokenExpires();
    }

    @Test
    void get() {
        // given
        Account account = mock(Account.class);
        given(accountRepository.findByUsernameAndUseYnIsTrue("username")).willReturn(Optional.of(account));

        // when
        Account result = accountService.get("username");

        // then
        assertThat(result).isEqualTo(account);
    }

    @Test
    void signupMember() {
        // given
        Account account = mock(Account.class);
        given(accountRepository.findByUsernameAndUseYnIsTrue("username")).willReturn(Optional.of(account));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        // when
        accountService.signupMember("username", 1L);

        // then
        then(account).should().setMember(member);
    }

    @Test
    void delete() {
        // given
        Account account = mock(Account.class);
        given(accountRepository.findByUsernameAndUseYnIsTrue("username")).willReturn(Optional.of(account));

        // when
        accountService.delete("username");

        // then
        then(account).should().delete();
    }

}