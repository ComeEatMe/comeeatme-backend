package com.comeeatme.domain.comment;


import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.comment.repository.CommentRepository;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatNoException;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class CommentTest {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("Comment 생성 및 저장")
    void createAndSave() {
        assertThatNoException().isThrownBy(() -> commentRepository.save(Comment.builder()
                .member(memberRepository.getReferenceById(1L))
                .post(Post.builder().id(2L).build())
                .content("test-content")
                .build()
        ));
    }

}