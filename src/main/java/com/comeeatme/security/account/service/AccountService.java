package com.comeeatme.security.account.service;

import com.comeeatme.security.account.Account;
import com.comeeatme.security.account.repository.AccountRepository;
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Long getMemberId(String username) {
        Account account = getAccountByUsername(username);
        return account.getMember().getId();
    }

    @Transactional
    public Account login(String username, String refreshToken, LocalDateTime refreshTokenExpiresAt) {
        Account account = getOrCreate(username);
        account.renewRefreshToken(refreshToken, refreshTokenExpiresAt);
        return account;
    }

    @Transactional
    public void logout(String username) {
        Account account = getAccountByUsername(username);
        account.refreshTokenExpires();
    }

    private Account getOrCreate(String username) {
        Optional<Account> accountOptional = accountRepository.findByUsername(username);
        if (accountOptional.isEmpty()) {
            Account account = accountRepository.save(Account.builder()
                    .username(username)
                    .build());
            accountOptional = Optional.of(account);
        }
        return accountOptional.get();
    }

    private Account getAccountByUsername(String username) {
        return accountRepository.findByUsername(username)
                .filter(Account::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Account.username=" + username));
    }

}
