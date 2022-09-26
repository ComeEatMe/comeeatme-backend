package com.comeeatme.domain.member.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.account.Account;
import com.comeeatme.domain.account.repository.AccountRepository;
import com.comeeatme.domain.member.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class MemberRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void findByUsername() {
        // given
        Member member = memberRepository.saveAndFlush(Member.builder()
                .nickname("test-nickname")
                .introduction("test-introduction")
                .profileImageUrl("test-profileimageurl")
                .build());
        accountRepository.saveAndFlush(Account.builder()
                .username("test-username")
                .member(member)
                .build());

        // when
        Member foundMember = memberRepository.findByUsername("test-username").orElseThrow();

        // then
        assertThat(foundMember.getNickname()).isEqualTo("test-nickname");
        assertThat(foundMember.getIntroduction()).isEqualTo("test-introduction");
        assertThat(foundMember.getProfileImageUrl()).isEqualTo("test-profileimageurl");
    }
}