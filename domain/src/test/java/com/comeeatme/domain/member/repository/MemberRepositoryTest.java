package com.comeeatme.domain.member.repository;

import com.comeeatme.domain.common.TestJpaConfig;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.image.repository.ImageRepository;
import com.comeeatme.domain.member.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManagerFactory;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private EntityManagerFactory emf;

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
        Image image = imageRepository.saveAndFlush(Image.builder()
                .originName("origin-name")
                .storedName("stored-name")
                .url("image-url")
                .member(memberRepository.getReferenceById(10L))
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
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<Member> result = memberRepository.findSliceWithImagesByNicknameStartingWith(pageRequest, "nick");

        // then
        List<Member> content = result.getContent();
        assertThat(content)
                .hasSize(1)
                .extracting("id").containsExactly(member1.getId());
        assertThat(emf.getPersistenceUnitUtil()
                .isLoaded(content.get(0).getImage())).isTrue();
    }

}