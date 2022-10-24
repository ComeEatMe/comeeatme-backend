package com.comeeatme.domain.post.service;

import com.comeeatme.domain.comment.Comment;
import com.comeeatme.domain.comment.repository.CommentRepository;
import com.comeeatme.domain.comment.response.CommentCount;
import com.comeeatme.domain.images.Images;
import com.comeeatme.domain.images.repository.ImagesRepository;
import com.comeeatme.domain.likes.repository.LikesRepository;
import com.comeeatme.domain.likes.response.LikeCount;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.PostEditor;
import com.comeeatme.domain.post.PostHashtag;
import com.comeeatme.domain.post.PostImage;
import com.comeeatme.domain.post.repository.PostImageRepository;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.post.request.PostCreate;
import com.comeeatme.domain.post.request.PostEdit;
import com.comeeatme.domain.post.request.PostSearch;
import com.comeeatme.domain.post.response.PostDto;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    private final PostImageRepository postImageRepository;

    private final ImagesRepository imagesRepository;

    private final RestaurantRepository restaurantRepository;

    private final MemberRepository memberRepository;

    private final CommentRepository commentRepository;

    private final LikesRepository likesRepository;

    @Transactional
    public Long create(PostCreate postCreate, String username) {
        Member member = getMemberByUsername(username);
        Restaurant restaurant = getRestaurantById(postCreate.getRestaurantId());
        Post post = postRepository.save(Post.builder()
                .member(member)
                .restaurant(restaurant)
                .content(postCreate.getContent())
                .build());
        postCreate.getHashtags().forEach(post::addHashtag);
        savePostImages(post, postCreate.getImageIds());
        return post.getId();
    }

    @Transactional
    public Long edit(PostEdit postEdit, Long postId) {
        Post post = getPostById(postId);
        Set<PostHashtag> editedPostHashtags = postEdit.getHashtags()
                .stream()
                .map(hashtag -> PostHashtag.builder()
                        .post(post)
                        .hashtag(hashtag)
                        .build())
                .collect(Collectors.toSet());
        PostEditor.PostEditorBuilder editorBuilder = post.toEditor()
                .postHashtags(editedPostHashtags)
                .content(postEdit.getContent());

        if (!Objects.equals(post.getRestaurant().getId(), postEdit.getRestaurantId())) {
            Restaurant restaurant = getRestaurantById(postEdit.getRestaurantId());
            editorBuilder.restaurant(restaurant);
        }

        PostEditor editor = editorBuilder.build();
        post.edit(editor);
        return post.getId();
    }

    @Transactional
    public Long delete(Long postId) {
        Post post = getPostById(postId);
        commentRepository.findAllByPostAndUseYnIsTrue(post)
                .forEach(Comment::delete);
        post.delete();
        return post.getId();
    }

    public Slice<PostDto> getList(Pageable pageable, PostSearch postSearch) {
        Slice<Post> posts = postRepository.findAllWithMemberAndRestaurant(pageable, postSearch);
        List<PostImage> postImages = postImageRepository.findAllWithImagesByPostInAndUseYnIsTrue(posts.getContent());
        Map<Long, List<PostImage>> postIdToPostImages = postImages.stream()
                .collect(Collectors.groupingBy(postImage -> postImage.getPost().getId()));
        Map<Long, CommentCount> postIdToCommentCount = commentRepository.countsGroupByPosts(posts.getContent())
                .stream()
                .collect(Collectors.toMap(CommentCount::getPostId, Function.identity()));
        Map<Long, LikeCount> postIdToLikeCount = likesRepository.countsGroupByPosts(posts.getContent())
                .stream()
                .collect(Collectors.toMap(LikeCount::getPostId, Function.identity()));
        return posts.map(post -> PostDto.of(post,
                postIdToPostImages.get(post.getId()),
                postIdToCommentCount.get(post.getId()),
                postIdToLikeCount.get(post.getId())
        ));
    }

    public boolean isNotOwnedByMember(Long postId, String username) {
        return !postRepository.existsByIdAndUsernameAndUseYnIsTrue(postId, username);
    }

    private Member getMemberByUsername(String username) {
        return memberRepository.findByUsername(username)
                .filter(Member::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Member username=" + username));
    }

    private Restaurant getRestaurantById(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .filter(Restaurant::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant id=" + restaurantId));
    }

    private List<Images> getImagesByIds(List<Long> imageIds) {
        return imagesRepository.findAllById(imageIds)
                .stream()
                .filter(Images::getUseYn)
                .collect(Collectors.toList());
    }

    private void savePostImages(Post post, List<Long> imageIds) {
        List<Images> images = getImagesByIds(imageIds);
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
