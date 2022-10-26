package com.comeeatme.domain.likes.service;

import com.comeeatme.domain.likes.Likes;
import com.comeeatme.domain.likes.repository.LikesRepository;
import com.comeeatme.domain.likes.response.LikeResult;
import com.comeeatme.domain.likes.response.LikedResult;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public LikeResult pushLike(Long postId, String username) {
        Post post = getPostById(postId);
        Member member = getMemberByUsername(username);
        Likes like = likesRepository.findByPostAndMember(post, member)
                .orElse(null);

        if (isNull(like)) {
            return like(post, member);
        } else {
            return unlike(post, like);
        }
    }

    private LikeResult like(Post post, Member member) {
        likesRepository.save(Likes.builder()
                .post(post)
                .member(member)
                .build());
        return LikeResult.builder()
                .postId(post.getId())
                .liked(true)
                .count(likesRepository.countByPost(post))
                .build();
    }

    private LikeResult unlike(Post post, Likes like) {
        likesRepository.delete(like);
        return LikeResult.builder()
                .postId(post.getId())
                .liked(false)
                .count(likesRepository.countByPost(post))
                .build();
    }

    public List<LikedResult> isLiked(List<Long> postIds, String username) {
        return likesRepository.existsByPostIdsAndUsername(postIds, username);
    }

    public List<LikedResult> isLiked(List<Long> postIds, Long memberId) {
        return likesRepository.existsByPostIdsAndMemberId(postIds, memberId);
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
