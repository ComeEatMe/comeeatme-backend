package com.comeeatme.domain.bookmark.service;

import com.comeeatme.domain.bookmark.Bookmark;
import com.comeeatme.domain.bookmark.BookmarkGroup;
import com.comeeatme.domain.bookmark.repository.BookmarkGroupRepository;
import com.comeeatme.domain.bookmark.repository.BookmarkRepository;
import com.comeeatme.domain.bookmark.response.BookmarkGroupDto;
import com.comeeatme.domain.bookmark.response.BookmarkedPostDto;
import com.comeeatme.domain.bookmark.response.PostBookmarked;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.PostImage;
import com.comeeatme.domain.post.repository.PostImageRepository;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.error.exception.AlreadyBookmarkedException;
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkGroupRepository bookmarkGroupRepository;

    private final BookmarkRepository bookmarkRepository;

    private final PostRepository postRepository;

    private final PostImageRepository postImageRepository;

    private final MemberRepository memberRepository;

    @Transactional
    public void bookmark(Long postId, Long memberId, String groupName) {
        Post post = getPostWithPessimisticLockById(postId);
        Member member = getMemberById(memberId);
        BookmarkGroup group = getBookmarkGroupByMemberAndName(member, groupName);
        if (bookmarkRepository.existsByMemberAndGroupAndPost(member, group, post)) {
            throw new AlreadyBookmarkedException(String.format(
                    "member.id=%s, bookmark.group=%s, post.id=%s",
                    member.getId(), groupName, post.getId()
            ));
        }

        bookmarkRepository.save(Bookmark.builder()
                .member(member)
                .group(group)
                .post(post)
                .build());
        Optional.ofNullable(group).ifPresent(BookmarkGroup::incrBookmarkCount);
        post.increaseBookmarkCount();
    }

    @Transactional
    public void cancelBookmark(Long postId, Long memberId, String groupName) {
        Post post = getPostById(postId);
        Member member = getMemberById(memberId);
        BookmarkGroup group = getBookmarkGroupByMemberAndName(member, groupName);
        Bookmark bookmark = bookmarkRepository.findByMemberAndGroupAndPost(member, group, post)
                .orElseThrow(() -> new EntityNotFoundException("group=" + groupName + ", post.id=" + postId));
        bookmarkRepository.delete(bookmark);
        Optional.ofNullable(group).ifPresent(BookmarkGroup::decrBookmarkCount);
    }

    public List<BookmarkGroupDto> getAllGroupsOfMember(Long memberId) {
        Member member = getMemberById(memberId);
        List<BookmarkGroup> groups = bookmarkGroupRepository.findAllByMember(member);
        int allCount = bookmarkRepository.countByMember(member);
        List<BookmarkGroupDto> groupDtos = new ArrayList<>();
        groupDtos.add(BookmarkGroupDto.builder()
                .name(BookmarkGroup.ALL_NAME)
                .bookmarkCount(allCount)
                .build());
        groupDtos.addAll(groups.stream().map(group -> BookmarkGroupDto.builder()
                        .name(group.getName())
                        .bookmarkCount(group.getBookmarkCount())
                        .build())
                .collect(Collectors.toList()));
        return groupDtos;
    }

    public Slice<BookmarkedPostDto> getBookmarkedPosts(Pageable pageable, Long memberId, @Nullable String groupName) {
        Member member = getMemberById(memberId);
        BookmarkGroup group = getBookmarkGroupByMemberAndName(member, groupName);
        Slice<Post> bookmarkedPosts = bookmarkRepository.findSliceWithByMemberAndGroup(pageable, member, group)
                .map(Bookmark::getPost);
        Map<Long, List<PostImage>> postIdToPostImages = postImageRepository.findAllWithImageByPostIn(
                        bookmarkedPosts.getContent())
                .stream()
                .filter(postImage -> postImage.getImage().getUseYn())
                .collect(Collectors.groupingBy(postImage -> postImage.getPost().getId()));
        return bookmarkedPosts
                .map(post -> BookmarkedPostDto.builder()
                        .id(post.getId())
                        .content(post.getContent())
                        .createdAt(post.getCreatedAt())
                        .imageUrls(postIdToPostImages.get(post.getId()).stream()
                                .map(postImage -> postImage.getImage().getUrl())
                                .collect(Collectors.toList()))
                        .memberId(post.getMember().getId())
                        .memberNickname(post.getMember().getNickname())
                        .memberImageUrl(Optional.ofNullable(post.getMember().getImage())
                                .filter(Image::getUseYn)
                                .map(Image::getUrl)
                                .orElse(null))
                        .restaurantId(post.getRestaurant().getId())
                        .restaurantName(post.getRestaurant().getName())
                        .build()
                );
    }

    public List<PostBookmarked> areBookmarked(Long memberId, List<Long> postIds) {
        List<Bookmark> bookmarks = bookmarkRepository.findByMemberIdAndPostIds(memberId, postIds);
        Set<Long> existentPostIds = bookmarks.stream()
                .map(bookmark -> bookmark.getPost().getId())
                .collect(Collectors.toSet());
        return postIds.stream()
                .map(postId -> PostBookmarked.builder()
                        .postId(postId)
                        .bookmarked(existentPostIds.contains(postId))
                        .build()
                ).collect(Collectors.toList());
    }

    public boolean isBookmarked(Long memberId, Long postId) {
        Member member = getMemberById(memberId);
        Post post = getPostById(postId);
        return bookmarkRepository.existsByMemberAndPost(member, post);
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

    @Nullable
    private BookmarkGroup getBookmarkGroupByMemberAndName(Member member, @Nullable String name) {
        return Optional.ofNullable(name)
                .map(groupName -> bookmarkGroupRepository.findByMemberAndName(member, groupName)
                        .orElseThrow(() -> new EntityNotFoundException(String.format(
                                "BookmarkGroup member.id=%s, name=%s", member.getId(), groupName)))
                ).orElse(null);
    }

}
