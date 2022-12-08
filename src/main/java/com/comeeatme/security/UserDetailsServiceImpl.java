package com.comeeatme.security;

import com.comeeatme.security.account.Account;
import com.comeeatme.security.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final String OAUTH2_PASSWORD = "OAUTH2-ACCOUNT";

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username)
                .filter(Account::getUseYn)
                .orElseThrow(() -> new UsernameNotFoundException("username = " + username));

        return User.withUsername(account.getUsername())
                .password(Optional.ofNullable(account.getPassword()).orElse(OAUTH2_PASSWORD))
                .authorities(AuthorityUtils.NO_AUTHORITIES)
                .build();
    }
}
