package com.comeeatme.domain.member.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.account.Account;
import com.comeeatme.domain.account.repository.AccountRepository;
import com.comeeatme.domain.images.Images;
import com.comeeatme.domain.images.repository.ImagesRepository;
import com.comeeatme.domain.member.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class MemberRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ImagesRepository imagesRepository;

    @Autowired
    private EntityManager em;

    @Test
    void findByUsername() {
        // given
        Member member = memberRepository.saveAndFlush(Member.builder()
                .nickname("test-nickname")
                .introduction("test-introduction")
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
    }

    @Test
    void findByUsername_AccountDeleted() {
        // given
        Member member = memberRepository.saveAndFlush(Member.builder()
                .nickname("test-nickname")
                .introduction("test-introduction")
                .build());
        Account account = accountRepository.saveAndFlush(Account.builder()
                .username("test-username")
                .member(member)
                .build());
        account.delete();

        // expected
        assertThat(memberRepository.findByUsername("test-username")).isEmpty();
    }

    @Test
    void existsByNickname_True() {
        // given
        memberRepository.saveAndFlush(Member.builder()
                .nickname("test-nickname")
                .introduction("test-introduction")
                .build());

        // expected
        assertThat(memberRepository.existsByNickname("test-nickname")).isTrue();
    }

    @Test
    void existsByNickname_False() {
        // given
        memberRepository.saveAndFlush(Member.builder()
                .nickname("test-nickname")
                .introduction("test-introduction")
                .build());

        // expected
        assertThat(memberRepository.existsByNickname("not-duplicate")).isFalse();
    }

    @Test
    void findSliceWithImagesByNicknameStartingWith() {
        // given
        Images image = imagesRepository.saveAndFlush(Images.builder()
                .originName("origin-name")
                .storedName("stored-name")
                .url("image-url")
                .member(Member.builder().id(10L).build())
                .build());
        Member member1 = memberRepository.saveAndFlush(Member.builder()
                .nickname("nickname-1")
                .introduction("introduction-1")
                .image(image)
                .build());
        Member member2 = memberRepository.saveAndFlush(Member.builder()
                .nickname("test-nickname-2")
                .introduction("introduction-2")
                .build());

        // when
        Slice<Member> result = memberRepository.findSliceWithImagesByNicknameStartingWith("nick");

        // then
        List<Member> content = result.getContent();
        assertThat(content)
                .hasSize(1)
                .extracting("id").containsExactly(member1.getId());
        assertThat(em.getEntityManagerFactory().getPersistenceUnitUtil()
                .isLoaded(content.get(0).getImage())).isTrue();
    }
}