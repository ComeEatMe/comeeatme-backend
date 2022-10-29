package com.comeeatme.domain.bookmark.service;

import com.comeeatme.domain.bookmark.Bookmark;
import com.comeeatme.domain.bookmark.BookmarkGroup;
import com.comeeatme.domain.bookmark.repository.BookmarkGroupRepository;
import com.comeeatme.domain.bookmark.repository.BookmarkRepository;
import com.comeeatme.domain.bookmark.response.BookmarkGroupDto;
import com.comeeatme.domain.bookmark.response.PostBookmarked;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.error.exception.AlreadyBookmarkedException;
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkGroupRepository bookmarkGroupRepository;

    private final BookmarkRepository bookmarkRepository;

    private final PostRepository postRepository;

    private final MemberRepository memberRepository;

    @Transactional
    public void bookmark(Long postId, String username, String groupName) {
        Post post = getPostById(postId);
        Member member = getMemberByUsername(username);
        BookmarkGroup group = getBookmarkGroupByMemberAndName(member, groupName);
        if (bookmarkRepository.existsByGroupAndPost(group, post)) {
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
    }

    @Transactional
    public void cancelBookmark(Long postId, String username, String groupName) {
        Post post = getPostById(postId);
        Member member = getMemberByUsername(username);
        BookmarkGroup group = getBookmarkGroupByMemberAndName(member, groupName);
        Bookmark bookmark = bookmarkRepository.findByGroupAndPost(group, post)
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

    public List<PostBookmarked> isBookmarked(Long memberId, List<Long> postIds) {
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
