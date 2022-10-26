package com.comeeatme.domain.post.repository;

import com.comeeatme.common.TestJpaConfig;
import com.comeeatme.domain.account.repository.AccountRepository;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional
@Import(TestJpaConfig.class)
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

}