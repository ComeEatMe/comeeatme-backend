package com.comeeatme.domain.like.service;

import com.comeeatme.domain.like.Like;
import com.comeeatme.domain.like.repository.LikeRepository;
import com.comeeatme.domain.like.response.LikedPostDto;
import com.comeeatme.domain.like.response.PostLiked;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.PostImage;
import com.comeeatme.domain.post.repository.PostImageRepository;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.error.exception.AlreadyLikedPostException;
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;

    private final PostRepository postRepository;

    private final PostImageRepository postImageRepository;

    private final MemberRepository memberRepository;

    @Transactional
    public void like(Long postId, Long memberId) {
        Post post = getPostWithPessimisticLockById(postId);
        Member member = getMemberById(memberId);
        if (likeRepository.existsByPostAndMember(post, member)) {
            throw new AlreadyLikedPostException(String.format(
                    "post.id=%s, member.id=%s", post.getId(), member.getId()));
        }
        likeRepository.save(Like.builder()
                .post(post)
                .member(member)
                .build());
        post.increaseLikeCount();
    }

    @Transactional
    public void unlike(Long postId, Long memberId) {
        Post post = getPostWithPessimisticLockById(postId);
        Member member = getMemberById(memberId);
        Like like = likeRepository.findByPostAndMember(post, member)
                .orElseThrow(() -> new EntityNotFoundException(String.format(
                        "post.id=%s, member.id=%s", post.getId(), member.getId())));
        likeRepository.delete(like);
        post.decreaseLikeCount();
    }

    public List<PostLiked> areLiked(Long memberId, List<Long> postIds) {
        List<Like> likes = likeRepository.findByMemberIdAndPostIds(memberId, postIds);
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

    public boolean isLiked(Long memberId, Long postId) {
        Member member = getMemberById(memberId);
        Post post = getPostById(postId);
        return likeRepository.existsByPostAndMember(post, member);
    }

    public Slice<LikedPostDto> getLikedPosts(Pageable pageable, Long memberId) {
        Member member = getMemberById(memberId);
        Slice<Post> likedPosts = likeRepository.findSliceWithPostByMember(pageable, member)
                .map(Like::getPost);
        Map<Long, List<PostImage>> postIdToPostImages = postImageRepository.findAllWithImageByPostIn(
                        likedPosts.getContent())
                .stream()
                .filter(postImage -> postImage.getImage().getUseYn())
                .collect(Collectors.groupingBy(postImage -> postImage.getPost().getId()));

        return likedPosts
                .map(post -> LikedPostDto.builder()
                        .id(post.getId())
                        .content(post.getContent())
                        .createdAt(post.getCreatedAt())
                        .imageUrls(postIdToPostImages.get(post.getId()).stream()
                                .map(postImage -> postImage.getImage().getUrl())
                                .collect(Collectors.toList())
                        ).build()
                );
    }

    @Transactional
    public void deleteAllOfMember(Long memberId) {
        Member member = getMemberById(memberId);
        List<Like> likes = likeRepository.findAllByMember(member);
        List<Long> postIds = likes.stream()
                .map(like -> like.getPost().getId())
                .collect(Collectors.toList());
        likeRepository.deleteAll(likes);
        List<Post> posts = postRepository.findAllWithPessimisticLockByIdIn(postIds);
        posts.forEach(Post::decreaseLikeCount);
    }

    private Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .filter(Post::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Post id=" + postId));
    }

    private Post getPostWithPessimisticLockById(Long postId) {
        return postRepository.findWithPessimisticLockById(postId)
                .filter(Post::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Post id=" + postId));
    }

    private Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .filter(Member::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Member.id=" + id));
    }

}
