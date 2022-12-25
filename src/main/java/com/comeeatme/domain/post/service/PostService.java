package com.comeeatme.domain.post.service;

import com.comeeatme.domain.bookmark.repository.BookmarkRepository;
import com.comeeatme.domain.comment.Comment;
import com.comeeatme.domain.comment.repository.CommentRepository;
import com.comeeatme.domain.common.response.CreateResult;
import com.comeeatme.domain.common.response.DeleteResult;
import com.comeeatme.domain.common.response.UpdateResult;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.image.repository.ImageRepository;
import com.comeeatme.domain.like.repository.LikeRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.*;
import com.comeeatme.domain.post.repository.PostImageRepository;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.post.request.PostCreate;
import com.comeeatme.domain.post.request.PostEdit;
import com.comeeatme.domain.post.request.PostSearch;
import com.comeeatme.domain.post.response.MemberPostDto;
import com.comeeatme.domain.post.response.PostDetailDto;
import com.comeeatme.domain.post.response.PostDto;
import com.comeeatme.domain.post.response.RestaurantPostDto;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final ImageRepository imageRepository;
    private final RestaurantRepository restaurantRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final BookmarkRepository bookmarkRepository;

    @Transactional
    public CreateResult<Long> create(PostCreate postCreate, Long memberId) {
        Member member = getMemberById(memberId);
        Restaurant restaurant = getRestaurantWithPessimisticLockById(postCreate.getRestaurantId());
        Post post = postRepository.save(Post.builder()
                .member(member)
                .restaurant(restaurant)
                .content(postCreate.getContent())
                .build());
        postCreate.getHashtags().forEach(post::addHashtag);
        savePostImages(post, postCreate.getImageIds());
        restaurant.increasePostCount();
        return new CreateResult<>(post.getId());
    }

    @Transactional
    public UpdateResult<Long> edit(PostEdit postEdit, Long postId) {
        Post post = getPostById(postId);

        PostEditor.PostEditorBuilder editorBuilder = post.toEditor()
                .content(postEdit.getContent());

        if (!Objects.equals(post.getRestaurant().getId(), postEdit.getRestaurantId())) {
            getRestaurantWithPessimisticLockById(post.getRestaurant().getId()).decreasePostCount();

            Restaurant editedRestaurant = getRestaurantWithPessimisticLockById(postEdit.getRestaurantId());
            editedRestaurant.increasePostCount();
            editorBuilder.restaurant(editedRestaurant);
        }

        PostEditor editor = editorBuilder.build();
        post.edit(editor);

        Set<Hashtag> hashtags = new HashSet<>(post.getHashtags());
        postEdit.getHashtags().stream()
                .filter(hashtag -> !hashtags.contains(hashtag))
                .forEach(post::addHashtag);
        List<PostHashtag> deletedPostHashtags = post.getPostHashtags().stream()
                .filter(postHashtag -> !postEdit.getHashtags().contains(postHashtag.getHashtag()))
                .collect(Collectors.toList());
        deletedPostHashtags.forEach(deletedPostHashtag -> post.getPostHashtags().remove(deletedPostHashtag));
        return new UpdateResult<>(post.getId());
    }

    @Transactional
    public DeleteResult<Long> delete(Long postId) {
        Post post = getPostById(postId);
        commentRepository.findAllByPostAndUseYnIsTrue(post)
                .forEach(Comment::delete);
        postImageRepository.findAllWithImageByPost(post)
                .forEach(postImage -> postImage.getImage().delete());
        likeRepository.deleteAllByPost(post);
        bookmarkRepository.deleteAllByPost(post);
        Restaurant restaurant = getRestaurantWithPessimisticLockById(post.getRestaurant().getId());
        post.delete();
        restaurant.decreasePostCount();
        return new DeleteResult<>(post.getId());
    }

    public Slice<PostDto> getList(Pageable pageable, PostSearch postSearch) {
        Slice<Post> posts = postRepository.findSliceWithMemberAndRestaurantBy(pageable, postSearch);
        Map<Long, List<PostImage>> postIdToPostImages = getPostIdToPostImages(posts.getContent());

        return posts.map(
                post -> PostDto.builder()
                        .id(post.getId())
                        .imageUrls(postIdToPostImages.getOrDefault(post.getId(), Collections.emptyList())
                                .stream()
                                .map(PostImage::getImage)
                                .map(Image::getUrl)
                                .collect(Collectors.toList()))
                        .content(post.getContent())
                        .createdAt(post.getCreatedAt())
                        .commentCount(post.getCommentCount())
                        .likeCount(post.getLikeCount())
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

    public Slice<MemberPostDto> getListOfMember(Pageable pageable, Long memberId) {
        Member member = getMemberById(memberId);
        Slice<Post> posts = postRepository.findSliceWithRestaurantByMemberAndUseYnIsTrue(pageable, member);
        Map<Long, List<PostImage>> postIdToPostImages = getPostIdToPostImages(posts.getContent());

        return posts.map(post -> MemberPostDto.builder()
                .id(post.getId())
                .imageUrls(postIdToPostImages.getOrDefault(post.getId(), Collections.emptyList())
                        .stream()
                        .map(PostImage::getImage)
                        .map(Image::getUrl)
                        .collect(Collectors.toList()))
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .commentCount(post.getCommentCount())
                .likeCount(post.getLikeCount())
                .restaurantId(post.getRestaurant().getId())
                .restaurantName(post.getRestaurant().getName())
                .build());
    }

    public Slice<RestaurantPostDto> getListOfRestaurant(Pageable pageable, Long restaurantId) {
        Restaurant restaurant = getRestaurantById(restaurantId);
        Slice<Post> posts = postRepository.findSliceWithMemberByRestaurantAndUseYnIsTrue(pageable, restaurant);
        Map<Long, List<PostImage>> postIdToPostImages = getPostIdToPostImages(posts.getContent());

        return posts
                .map(post -> RestaurantPostDto.builder()
                        .id(post.getId())
                        .imageUrls(postIdToPostImages.getOrDefault(post.getId(), Collections.emptyList())
                                .stream()
                                .map(PostImage::getImage)
                                .map(Image::getUrl)
                                .collect(Collectors.toList()))
                        .content(post.getContent())
                        .createdAt(post.getCreatedAt())
                        .memberId(post.getMember().getId())
                        .memberNickname(post.getMember().getNickname())
                        .memberImageUrl(Optional.ofNullable(post.getMember().getImage())
                                .filter(Image::getUseYn)
                                .map(Image::getUrl)
                                .orElse(null))
                        .build()
                );
    }

    public PostDetailDto get(Long postId) {
        Post post = getPostById(postId);
        List<String> postImageUrls = postImageRepository.findAllWithImageByPost(post).stream()
                .filter(postImage -> postImage.getImage().getUseYn())
                .map(postImage -> postImage.getImage().getUrl())
                .collect(Collectors.toList());

        return PostDetailDto.builder()
                .id(post.getId())
                .imageUrls(postImageUrls)
                .content(post.getContent())
                .hashtags(post.getHashtags())
                .createdAt(post.getCreatedAt())
                .commentCount(post.getCommentCount())
                .likeCount(post.getLikeCount())
                .memberId(post.getMember().getId())
                .memberNickname(post.getMember().getNickname())
                .memberImageUrl(Optional.ofNullable(post.getMember().getImage())
                        .filter(Image::getUseYn)
                        .map(Image::getUrl)
                        .orElse(null))
                .restaurantId(post.getRestaurant().getId())
                .restaurantName(post.getRestaurant().getName())
                .restaurantAddressName(post.getRestaurant().getAddress().getName())
                .build();
    }

    public boolean isNotOwnedByMember(Long postId, Long memberId) {
        Member member = memberRepository.getReferenceById(memberId);
        return !postRepository.existsByIdAndMember(postId, member);
    }

    @Transactional
    public void deleteAllOfMember(Long memberId) {
        Member member = getMemberById(memberId);
        List<Post> posts = postRepository.findAllByMemberAndUseYnIsTrue(member);
        commentRepository.updateUseYnFalseByPostIn(posts);
        posts.forEach(Post::delete);
        Map<Long, Long> restaurantIdToCount = posts.stream()
                .map(post -> post.getRestaurant().getId())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<Restaurant> restaurants = restaurantRepository.findAllWithPessimisticLockByIdIn(
                restaurantIdToCount.keySet());
        restaurants.forEach(restaurant -> {
            Long restaurantCount = restaurantIdToCount.get(restaurant.getId());
            for (int i = 0; i < restaurantCount; i++) {
                restaurant.decreasePostCount();
            }
        });
    }

    private Map<Long, List<PostImage>> getPostIdToPostImages(List<Post> posts) {
        return postImageRepository.findAllWithImageByPostIn(posts)
                .stream()
                .filter(postImage -> postImage.getImage().getUseYn())
                .collect(Collectors.groupingBy(postImage -> postImage.getPost().getId()));
    }

    private Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .filter(Member::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Member.id=" + id));
    }

    private Restaurant getRestaurantById(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .filter(Restaurant::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant id=" + restaurantId));
    }

    private Restaurant getRestaurantWithPessimisticLockById(Long restaurantId) {
        return restaurantRepository.findWithPessimisticLockById(restaurantId)
                .filter(Restaurant::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant id=" + restaurantId));
    }

    private List<Image> getImagesByIds(List<Long> imageIds) {
        return imageRepository.findAllById(imageIds)
                .stream()
                .filter(Image::getUseYn)
                .collect(Collectors.toList());
    }

    private void savePostImages(Post post, List<Long> imageIds) {
        List<Image> images = getImagesByIds(imageIds);
        postImageRepository.saveAll(images.stream()
                .map(image -> PostImage.builder()
                        .post(post)
                        .image(image)
                        .build())
                .collect(Collectors.toList()));
    }

    private Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .filter(Post::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Post id=" + postId));
    }

}
