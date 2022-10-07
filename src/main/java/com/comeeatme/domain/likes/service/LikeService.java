package com.comeeatme.domain.likes.service;

import com.comeeatme.domain.likes.Likes;
import com.comeeatme.domain.likes.repository.LikesRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.isNull;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class LikeService {

    private final LikesRepository likesRepository;

    private final PostRepository postRepository;

    private final MemberRepository memberRepository;

    @Transactional
    public boolean pushLike(Long postId, String username) {
        Post post = getPostById(postId);
        Member member = getMemberByUsername(username);
        Likes like = likesRepository.findByPostAndMember(post, member)
                .orElse(null);

        if (isNull(like)) {
            likesRepository.save(Likes.builder()
                    .post(post)
                    .member(member)
                    .build());
            return true;
        } else {
            likesRepository.delete(like);
            return false;
        }
    }

    private Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .filter(Post::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Post id=" + postId));
    }

    private Member getMemberByUsername(String username) {
        return memberRepository.findByUsername(username)
                .filter(Member::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Member username=" + username));
    }
}
