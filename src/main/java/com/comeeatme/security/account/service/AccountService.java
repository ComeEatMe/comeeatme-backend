package com.comeeatme.security.account.service;

import com.comeeatme.security.account.Account;
import com.comeeatme.security.account.repository.AccountRepository;
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Long getMemberId(String username) {
        Account account = getAccountByUsername(username);
        return account.getMember().getId();
    }

    private Account getAccountByUsername(String username) {
        return accountRepository.findByUsername(username)
                .filter(Account::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Account.username=" + username));
    }

}
