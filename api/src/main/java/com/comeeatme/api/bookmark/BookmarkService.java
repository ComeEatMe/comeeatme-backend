package com.comeeatme.api.bookmark;

import com.comeeatme.api.bookmark.response.BookmarkedPostDto;
import com.comeeatme.api.bookmark.response.PostBookmarked;
import com.comeeatme.api.exception.AlreadyBookmarkedException;
import com.comeeatme.api.exception.EntityNotFoundException;
import com.comeeatme.domain.bookmark.Bookmark;
import com.comeeatme.domain.bookmark.repository.BookmarkRepository;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.PostImage;
import com.comeeatme.domain.post.repository.PostImageRepository;
import com.comeeatme.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;

    private final PostRepository postRepository;

    private final PostImageRepository postImageRepository;

    private final MemberRepository memberRepository;

    @Transactional
    public void bookmark(Long postId, Long memberId) {
        Post post = getPostWithPessimisticLockById(postId);
        Member member = getMemberById(memberId);
        if (bookmarkRepository.existsByPostAndMember(post, member)) {
            throw new AlreadyBookmarkedException(String.format(
                    "member.id=%s, post.id=%s", member.getId(), post.getId()
            ));
        }

        bookmarkRepository.save(Bookmark.builder()
                .member(member)
                .post(post)
                .build());
        post.increaseBookmarkCount();
    }

    @Transactional
    public void cancelBookmark(Long postId, Long memberId) {
        Post post = getPostWithPessimisticLockById(postId);
        Member member = getMemberById(memberId);
        Bookmark bookmark = bookmarkRepository.findByPostAndMember(post, member)
                .orElseThrow(() -> new EntityNotFoundException(String.format(
                        "memberId=%s, postId=%s", memberId, postId
                )));
        bookmarkRepository.delete(bookmark);
        post.decreaseBookmarkCount();
    }

    public Slice<BookmarkedPostDto> getBookmarkedPosts(Pageable pageable, Long memberId) {
        Member member = getMemberById(memberId);
        Slice<Post> bookmarkedPosts = bookmarkRepository.findSliceWithByMember(pageable, member)
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
        Member member = memberRepository.getReferenceById(memberId);
        List<Post> posts = postIds.stream()
                .map(postRepository::getReferenceById)
                .collect(Collectors.toList());
        List<Bookmark> bookmarks = bookmarkRepository.findAllByMemberAndPostIn(member, posts);
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

    @Transactional
    public void deleteAllOfMember(Long memberId) {
        Member member = getMemberById(memberId);
        List<Bookmark> bookmarks = bookmarkRepository.findAllByMember(member);
        bookmarkRepository.deleteAll(bookmarks);
        List<Long> postIds = bookmarks.stream()
                .map(bookmark -> bookmark.getPost().getId())
                .collect(Collectors.toList());
        List<Post> posts = postRepository.findAllWithPessimisticLockByIdIn(postIds);
        posts.forEach(Post::decreaseBookmarkCount);
    }

    public boolean isBookmarked(Long memberId, Long postId) {
        Member member = getMemberById(memberId);
        Post post = getPostById(postId);
        return bookmarkRepository.existsByPostAndMember(post, member);
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
