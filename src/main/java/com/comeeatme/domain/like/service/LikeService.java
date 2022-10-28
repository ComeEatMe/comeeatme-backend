package com.comeeatme.domain.like.service;

import com.comeeatme.domain.like.Like;
import com.comeeatme.domain.like.repository.LikeRepository;
import com.comeeatme.domain.like.response.PostLiked;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.error.exception.AlreadyLikedPostException;
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likesRepository;

    private final PostRepository postRepository;

    private final MemberRepository memberRepository;

    @Transactional
    public void like(Long postId, String username) {
        Post post = getPostById(postId);
        Member member = getMemberByUsername(username);
        if (likesRepository.existsByPostAndMember(post, member)) {
            throw new AlreadyLikedPostException(String.format(
                    "post.id=%s, member.id=%s", post.getId(), member.getId()));
        }
        likesRepository.save(Like.builder()
                .post(post)
                .member(member)
                .build());
    }

    @Transactional
    public void unlike(Long postId, String username) {
        Post post = getPostById(postId);
        Member member = getMemberByUsername(username);
        Like like = likesRepository.findByPostAndMember(post, member)
                .orElseThrow(() -> new EntityNotFoundException(String.format(
                        "post.id=%s, member.id=%s", post.getId(), member.getId())));
        likesRepository.delete(like);
    }

    public List<PostLiked> isLiked(Long memberId, List<Long> postIds) {
        List<Like> likes = likesRepository.findByMemberIdAndPostIds(memberId, postIds);
        Set<Long> existingPostIds = likes.stream()
                .map(like -> like.getPost().getId())
                .collect(Collectors.toSet());
        return postIds.stream()
                .map(postId -> PostLiked.builder()
                        .postId(postId)
                        .liked(existingPostIds.contains(postId))
                        .build()
                ).collect(Collectors.toList());
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
