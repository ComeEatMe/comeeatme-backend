package com.comeeatme.api.account;

import com.comeeatme.api.exception.EntityNotFoundException;
import com.comeeatme.domain.account.Account;
import com.comeeatme.domain.account.repository.AccountRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
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

    private final MemberRepository memberRepository;

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
        Optional<Account> accountOptional = accountRepository.findByUsernameAndUseYnIsTrue(username);
        if (accountOptional.isEmpty()) {
            Account account = accountRepository.save(Account.builder()
                    .username(username)
                    .build());
            accountOptional = Optional.of(account);
        }
        Account account = accountOptional.get();
        if (Boolean.FALSE.equals(account.getUseYn())) {
            throw new EntityNotFoundException("deleted Account.username=" + username);
        }
        return account;
    }

    public Account get(String username) {
        return getAccountByUsername(username);
    }

    @Transactional
    public void signupMember(String username, Long memberId) {
        Account account = getAccountByUsername(username);
        Member member = getMemberById(memberId);
        account.setMember(member);
    }

    @Transactional
    public void delete(String username) {
        Account account = getAccountByUsername(username);
        account.delete();
    }

    private Account getAccountByUsername(String username) {
        return accountRepository.findByUsernameAndUseYnIsTrue(username)
                .orElseThrow(() -> new EntityNotFoundException("Account.username=" + username));
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .filter(Member::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Member.id=" + memberId));
    }

}
