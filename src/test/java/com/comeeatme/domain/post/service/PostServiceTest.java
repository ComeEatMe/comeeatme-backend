package com.comeeatme.domain.post.service;

import com.comeeatme.domain.comment.Comment;
import com.comeeatme.domain.comment.repository.CommentRepository;
import com.comeeatme.domain.comment.response.CommentCount;
import com.comeeatme.domain.common.response.CreateResult;
import com.comeeatme.domain.common.response.DeleteResult;
import com.comeeatme.domain.common.response.UpdateResult;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.image.repository.ImageRepository;
import com.comeeatme.domain.like.repository.LikeRepository;
import com.comeeatme.domain.like.response.LikeCount;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.Hashtag;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.PostImage;
import com.comeeatme.domain.post.repository.PostImageRepository;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.post.request.PostCreate;
import com.comeeatme.domain.post.request.PostEdit;
import com.comeeatme.domain.post.request.PostSearch;
import com.comeeatme.domain.post.response.PostDto;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private LikeRepository likesRepository;

    @Test
    void create() {
        // given
        PostCreate postCreate = PostCreate.builder()
                .restaurantId(1L)
                .hashtags(Set.of(Hashtag.STRONG_TASTE, Hashtag.CLEANLINESS))
                .imageIds(List.of(2L, 3L, 4L))
                .content("test-content")
                .build();

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findByUsername(anyString()))
                .willReturn(Optional.of(member));

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findById(postCreate.getRestaurantId()))
                .willReturn(Optional.of(restaurant));

        Post post = mock(Post.class);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        given(postRepository.save(postCaptor.capture())).willReturn(post);

        Image image1 = mock(Image.class);
        Image image2 = mock(Image.class);
        Image image3 = mock(Image.class);
        given(image1.getUseYn()).willReturn(true);
        given(image2.getUseYn()).willReturn(true);
        given(image3.getUseYn()).willReturn(true);
        given(imageRepository.findAllById(postCreate.getImageIds()))
                .willReturn(List.of(image1, image2, image3));

        given(post.getId()).willReturn(5L);

        // when
        CreateResult<Long> result = postService.create(postCreate, "test-username");

        // then
        Post capturedPost = postCaptor.getValue();
        assertThat(capturedPost.getMember()).isEqualTo(member);
        assertThat(capturedPost.getRestaurant()).isEqualTo(restaurant);
        assertThat(capturedPost.getContent()).isEqualTo(postCreate.getContent());

        ArgumentCaptor<Hashtag> hashtagCaptor = ArgumentCaptor.forClass(Hashtag.class);
        then(post).should(times(postCreate.getHashtags().size())).addHashtag(hashtagCaptor.capture());
        assertThat(hashtagCaptor.getAllValues()).containsOnly(Hashtag.STRONG_TASTE, Hashtag.CLEANLINESS);

        ArgumentCaptor<List<PostImage>> postImagesCaptor = ArgumentCaptor.forClass(List.class);
        then(postImageRepository).should().saveAll(postImagesCaptor.capture());
        List<PostImage> capturedPostImages = postImagesCaptor.getValue();
        assertThat(capturedPostImages).hasSize(3);
        assertThat(capturedPostImages).extracting("post")
                .containsExactly(post, post, post);
        assertThat(capturedPostImages).extracting("image")
                .containsExactly(image1, image2, image3);

        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    void edit() {
        // given
        Restaurant postRestaurant = mock(Restaurant.class);
        given(postRestaurant.getId()).willReturn(2L);

        Post post = Post.builder()
                .id(1L)
                .restaurant(postRestaurant)
                .content("post-content")
                .build();
        post.addHashtag(Hashtag.STRONG_TASTE);
        post.addHashtag(Hashtag.DATE);

        PostEdit postEdit = PostEdit.builder()
                .restaurantId(3L)
                .hashtags(Set.of(Hashtag.STRONG_TASTE, Hashtag.CLEANLINESS))
                .content("edited-content")
                .build();

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Restaurant editedRestaurant = mock(Restaurant.class);
        given(editedRestaurant.getId()).willReturn(3L);
        given(editedRestaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findById(3L)).willReturn(Optional.of(editedRestaurant));

        // when
        UpdateResult<Long> updateResult = postService.edit(postEdit, 1L);

        // then
        assertThat(post.getRestaurant().getId()).isEqualTo(3L);
        assertThat(post.getHashtags()).containsOnly(Hashtag.STRONG_TASTE, Hashtag.CLEANLINESS);
        assertThat(post.getContent()).isEqualTo("edited-content");
        assertThat(updateResult.getId()).isEqualTo(1L);
    }

    @Test
    void edit_EqualsRestaurantId() {
        // given
        Restaurant postRestaurant = mock(Restaurant.class);
        given(postRestaurant.getId()).willReturn(2L);

        Post post = Post.builder()
                .id(1L)
                .restaurant(postRestaurant)
                .content("post-content")
                .build();
        post.addHashtag(Hashtag.STRONG_TASTE);
        post.addHashtag(Hashtag.DATE);

        PostEdit postEdit = PostEdit.builder()
                .restaurantId(2L)
                .hashtags(Set.of(Hashtag.STRONG_TASTE, Hashtag.CLEANLINESS))
                .content("edited-content")
                .build();

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        UpdateResult<Long> updateResult = postService.edit(postEdit, 1L);

        // then
        assertThat(post.getRestaurant().getId()).isEqualTo(2L);
        assertThat(post.getHashtags()).containsOnly(Hashtag.STRONG_TASTE, Hashtag.CLEANLINESS);
        assertThat(post.getContent()).isEqualTo("edited-content");
        assertThat(updateResult.getId()).isEqualTo(1L);
        then(restaurantRepository).should(never()).findById(any());
    }

    @Test
    void isNotOwnedByMember_False() {
        // given
        given(postRepository.existsByIdAndUsernameAndUseYnIsTrue(1L, "test-username")).willReturn(true);

        // then
        assertThat(postService.isNotOwnedByMember(1L, "test-username")).isFalse();
    }

    @Test
    void isNotOwnedByMember_True() {
        // given
        given(postRepository.existsByIdAndUsernameAndUseYnIsTrue(1L, "test-username")).willReturn(false);

        // then
        assertThat(postService.isNotOwnedByMember(1L, "test-username")).isTrue();
    }

    @Test
    void delete() {
        // given
        Post post = mock(Post.class);
        given(post.getId()).willReturn(1L);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        List<Comment> comments = List.of(
                mock(Comment.class), mock(Comment.class), mock(Comment.class));
        given(commentRepository.findAllByPostAndUseYnIsTrue(post)).willReturn(comments);

        // when
        DeleteResult<Long> deleteResult = postService.delete(1L);

        // then
        comments.forEach(comment -> then(comment).should().delete());
        then(post).should().delete();
        assertThat(deleteResult.getId()).isEqualTo(1L);
    }

    @Test
    void getList() {
        // given
        Member member = mock(Member.class);
        Restaurant restaurant = mock(Restaurant.class);
        Post post = mock(Post.class);
        given(post.getId()).willReturn(1L);
        given(post.getMember()).willReturn(member);
        given(post.getRestaurant()).willReturn(restaurant);
        SliceImpl<Post> postSlice = new SliceImpl<>(List.of(post));
        given(postRepository.findAllWithMemberAndRestaurant(any(Pageable.class), any(PostSearch.class)))
                .willReturn(postSlice);

        Image image1 = mock(Image.class);
        given(image1.getUseYn()).willReturn(true);
        given(image1.getUrl()).willReturn("image-url-1");
        PostImage postImage1 = mock(PostImage.class);
        given(postImage1.getPost()).willReturn(post);
        given(postImage1.getImage()).willReturn(image1);

        Image image2 = mock(Image.class);
        given(image2.getUseYn()).willReturn(false);
        PostImage postImage2 = mock(PostImage.class);
        given(postImage2.getImage()).willReturn(image2);

        given(postImageRepository.findAllWithImageByPostIn(postSlice.getContent()))
                .willReturn(List.of(postImage1, postImage2));
        given(commentRepository.countsGroupByPosts(postSlice.getContent()))
                .willReturn(List.of(new CommentCount(1L, 10L)));
        given(likesRepository.countsGroupByPosts(postSlice.getContent()))
                .willReturn(List.of(new LikeCount(1L, 20L)));

        // when
        Slice<PostDto> result = postService.getList(PageRequest.of(0, 10), PostSearch.builder().build());

        // then
        List<PostDto> content = result.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting("id").containsExactly(1L);
        assertThat(content.get(0).getImageUrls()).containsExactly("image-url-1");
    }

}