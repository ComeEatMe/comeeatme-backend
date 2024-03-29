package com.comeeatme.api.post;

import com.comeeatme.api.common.response.CreateResult;
import com.comeeatme.api.common.response.DeleteResult;
import com.comeeatme.api.common.response.UpdateResult;
import com.comeeatme.api.post.request.PostCreate;
import com.comeeatme.api.post.request.PostEdit;
import com.comeeatme.api.post.response.MemberPostDto;
import com.comeeatme.api.post.response.PostDetailDto;
import com.comeeatme.api.post.response.PostDto;
import com.comeeatme.api.post.response.RestaurantPostDto;
import com.comeeatme.domain.address.Address;
import com.comeeatme.domain.bookmark.repository.BookmarkRepository;
import com.comeeatme.domain.comment.Comment;
import com.comeeatme.domain.comment.repository.CommentRepository;
import com.comeeatme.domain.image.Image;
import com.comeeatme.domain.image.repository.ImageRepository;
import com.comeeatme.domain.like.repository.LikeRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.post.*;
import com.comeeatme.domain.post.repository.PostImageRepository;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.post.request.PostSearch;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    private LikeRepository likeRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

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
        given(memberRepository.findById(10L))
                .willReturn(Optional.of(member));

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findWithPessimisticLockById(postCreate.getRestaurantId()))
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
        CreateResult<Long> result = postService.create(postCreate, 10L);

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

        then(restaurant).should().increasePostCount();

        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    void edit() {
        // given
        Restaurant postRestaurant = mock(Restaurant.class);
        given(postRestaurant.getId()).willReturn(2L);

        Post post = mock(Post.class);
        given(post.getId()).willReturn(1L);
        given(post.getRestaurant()).willReturn(postRestaurant);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        PostEditor.PostEditorBuilder editorBuilder = mock(PostEditor.PostEditorBuilder.class);
        given(post.toEditor()).willReturn(editorBuilder);
        given(editorBuilder.content("edited-content")).willReturn(editorBuilder);

        Restaurant lockRestaurant = mock(Restaurant.class);
        given(lockRestaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findWithPessimisticLockById(postRestaurant.getId()))
                .willReturn(Optional.of(lockRestaurant));

        Restaurant editedLockRestaurant = mock(Restaurant.class);
        given(editedLockRestaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findWithPessimisticLockById(3L)).willReturn(Optional.of(editedLockRestaurant));
        given(editorBuilder.restaurant(editedLockRestaurant)).willReturn(editorBuilder);

        PostEditor editor = mock(PostEditor.class);
        given(editorBuilder.build()).willReturn(editor);

        given(post.getHashtags()).willReturn(List.of(Hashtag.STRONG_TASTE, Hashtag.COST_EFFECTIVENESS));

        PostHashtag postHashtag1 = mock(PostHashtag.class);
        given(postHashtag1.getHashtag()).willReturn(Hashtag.STRONG_TASTE);
        PostHashtag postHashtag2 = mock(PostHashtag.class);
        given(postHashtag2.getHashtag()).willReturn(Hashtag.COST_EFFECTIVENESS);
        PostHashtag postHashtag3 = mock(PostHashtag.class);
        given(postHashtag3.getHashtag()).willReturn(Hashtag.CLEANLINESS);

        Set<PostHashtag> postHashtags = mock(Set.class);
        given(postHashtags.stream()).willReturn(Stream.of(postHashtag1, postHashtag2, postHashtag3));
        given(post.getPostHashtags())
                .willReturn(postHashtags);

        // when
        PostEdit postEdit = PostEdit.builder()
                .restaurantId(3L)
                .hashtags(Set.of(Hashtag.STRONG_TASTE, Hashtag.CLEANLINESS))
                .content("edited-content")
                .build();
        UpdateResult<Long> updateResult = postService.edit(postEdit, 1L);

        // then
        then(lockRestaurant).should(times(1)).decreasePostCount();
        then(editedLockRestaurant).should(times(1)).increasePostCount();
        then(post).should().edit(editor);
        then(post).should().addHashtag(Hashtag.CLEANLINESS);
        then(postHashtags).should().remove(postHashtag2);
        assertThat(updateResult.getId()).isEqualTo(1L);
    }

    @Test
    void edit_EqualsRestaurantId() {
        // given
        Restaurant postRestaurant = mock(Restaurant.class);
        given(postRestaurant.getId()).willReturn(2L);

        Post post = mock(Post.class);
        given(post.getId()).willReturn(1L);
        given(post.getRestaurant()).willReturn(postRestaurant);
        given(post.getUseYn()).willReturn(true);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        PostEditor.PostEditorBuilder editorBuilder = mock(PostEditor.PostEditorBuilder.class);
        given(post.toEditor()).willReturn(editorBuilder);
        given(editorBuilder.content("edited-content")).willReturn(editorBuilder);

        PostEditor editor = mock(PostEditor.class);
        given(editorBuilder.build()).willReturn(editor);

        given(post.getHashtags()).willReturn(List.of(Hashtag.STRONG_TASTE, Hashtag.COST_EFFECTIVENESS));

        PostHashtag postHashtag1 = mock(PostHashtag.class);
        given(postHashtag1.getHashtag()).willReturn(Hashtag.STRONG_TASTE);
        PostHashtag postHashtag2 = mock(PostHashtag.class);
        given(postHashtag2.getHashtag()).willReturn(Hashtag.COST_EFFECTIVENESS);
        PostHashtag postHashtag3 = mock(PostHashtag.class);
        given(postHashtag3.getHashtag()).willReturn(Hashtag.CLEANLINESS);

        Set<PostHashtag> postHashtags = mock(Set.class);
        given(postHashtags.stream()).willReturn(Stream.of(postHashtag1, postHashtag2, postHashtag3));
        given(post.getPostHashtags())
                .willReturn(postHashtags);

        // when
        PostEdit postEdit = PostEdit.builder()
                .restaurantId(2L)
                .hashtags(Set.of(Hashtag.STRONG_TASTE, Hashtag.CLEANLINESS))
                .content("edited-content")
                .build();
        UpdateResult<Long> updateResult = postService.edit(postEdit, 1L);

        // then
        then(editorBuilder).should(never()).restaurant(any());
        then(post).should().edit(editor);
        then(post).should().addHashtag(Hashtag.CLEANLINESS);
        then(postHashtags).should().remove(postHashtag2);
        assertThat(updateResult.getId()).isEqualTo(1L);
    }

    @Test
    void isNotOwnedByMember_False() {
        // given
        Member member = mock(Member.class);
        given(memberRepository.getReferenceById(2L)).willReturn(member);
        given(postRepository.existsByIdAndMember(1L, member)).willReturn(true);

        // then
        assertThat(postService.isNotOwnedByMember(1L, 2L)).isFalse();
    }

    @Test
    void isNotOwnedByMember_True() {
        // given
        Member member = mock(Member.class);
        given(memberRepository.getReferenceById(2L)).willReturn(member);
        given(postRepository.existsByIdAndMember(1L, member)).willReturn(false);

        // then
        assertThat(postService.isNotOwnedByMember(1L, 2L)).isTrue();
    }

    @Test
    void delete() {
        // given
        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(2L);

        Post post = mock(Post.class);
        given(post.getId()).willReturn(1L);
        given(post.getUseYn()).willReturn(true);
        given(post.getRestaurant()).willReturn(restaurant);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        List<Comment> comments = List.of(
                mock(Comment.class), mock(Comment.class), mock(Comment.class));
        given(commentRepository.findAllByPostAndUseYnIsTrue(post)).willReturn(comments);

        Restaurant lockedRestaurant = mock(Restaurant.class);
        given(lockedRestaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findWithPessimisticLockById(restaurant.getId())).willReturn(Optional.of(lockedRestaurant));

        // when
        DeleteResult<Long> deleteResult = postService.delete(1L);

        // then
        comments.forEach(comment -> then(comment).should().delete());
        then(likeRepository).should().deleteAllByPost(post);
        then(bookmarkRepository).should().deleteAllByPost(post);
        then(lockedRestaurant).should().decreasePostCount();
        then(post).should().delete();
        assertThat(deleteResult.getId()).isEqualTo(1L);
    }

    @Test
    void getList() {
        // given
        Image memberImage = mock(Image.class);
        given(memberImage.getUseYn()).willReturn(true);
        given(memberImage.getUrl()).willReturn("member-image-url");

        Member member = mock(Member.class);
        given(member.getId()).willReturn(2L);
        given(member.getNickname()).willReturn("nickname");
        given(member.getImage()).willReturn(memberImage);

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(3L);
        given(restaurant.getName()).willReturn("지그재그");

        Post post = mock(Post.class);
        given(post.getId()).willReturn(1L);
        given(post.getContent()).willReturn("content");
        given(post.getCommentCount()).willReturn(10);
        given(post.getLikeCount()).willReturn(20);
        given(post.getCreatedAt()).willReturn(LocalDateTime.of(2022, 11, 1, 1, 12));
        given(post.getMember()).willReturn(member);
        given(post.getRestaurant()).willReturn(restaurant);

        SliceImpl<Post> postSlice = new SliceImpl<>(List.of(post));
        given(postRepository.findSliceWithMemberAndRestaurantBy(any(Pageable.class), any(PostSearch.class)))
                .willReturn(postSlice);

        Image image1 = mock(Image.class);
        given(image1.getUseYn()).willReturn(true);
        given(image1.getUrl()).willReturn("image-url-1");
        PostImage postImage1 = mock(PostImage.class);
        given(postImage1.getPost()).willReturn(post);
        given(postImage1.getImage()).willReturn(image1);

        Image image2 = mock(Image.class);
        given(image2.getUseYn()).willReturn(true);
        given(image2.getUrl()).willReturn("image-url-2");
        PostImage postImage2 = mock(PostImage.class);
        given(postImage2.getPost()).willReturn(post);
        given(postImage2.getImage()).willReturn(image2);

        given(postImageRepository.findAllWithImageByPostIn(postSlice.getContent()))
                .willReturn(List.of(postImage1, postImage2));

        // when
        Slice<PostDto> result = postService.getList(PageRequest.of(0, 10), PostSearch.builder().build());

        // then
        List<PostDto> content = result.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting("id").containsExactly(1L);
        assertThat(content).extracting("content").containsExactly("content");
        assertThat(content).extracting("createdAt").containsExactly(LocalDateTime.of(2022, 11, 1, 1, 12));
        assertThat(content).extracting("commentCount").containsExactly(10);
        assertThat(content).extracting("likeCount").containsExactly(20);
        assertThat(content).extracting("member.id").containsExactly(2L);
        assertThat(content).extracting("member.nickname").containsExactly("nickname");
        assertThat(content).extracting("member.imageUrl").containsExactly("member-image-url");
        assertThat(content).extracting("restaurant.id").containsExactly(3L);
        assertThat(content).extracting("restaurant.name").containsExactly("지그재그");

        assertThat(content.get(0).getImageUrls()).containsExactly("image-url-1", "image-url-2");
    }

    @Test
    void getList_MemberImageNull() {
        // given
        Member member = mock(Member.class);
        given(member.getId()).willReturn(2L);
        given(member.getNickname()).willReturn("nickname");

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(3L);
        given(restaurant.getName()).willReturn("지그재그");

        Post post = mock(Post.class);
        given(post.getId()).willReturn(1L);
        given(post.getContent()).willReturn("content");
        given(post.getCommentCount()).willReturn(10);
        given(post.getLikeCount()).willReturn(20);
        given(post.getCreatedAt()).willReturn(LocalDateTime.of(2022, 11, 1, 1, 12));
        given(post.getMember()).willReturn(member);
        given(post.getRestaurant()).willReturn(restaurant);

        SliceImpl<Post> postSlice = new SliceImpl<>(List.of(post));
        given(postRepository.findSliceWithMemberAndRestaurantBy(any(Pageable.class), any(PostSearch.class)))
                .willReturn(postSlice);

        Image image1 = mock(Image.class);
        given(image1.getUseYn()).willReturn(true);
        given(image1.getUrl()).willReturn("image-url-1");
        PostImage postImage1 = mock(PostImage.class);
        given(postImage1.getPost()).willReturn(post);
        given(postImage1.getImage()).willReturn(image1);

        Image image2 = mock(Image.class);
        given(image2.getUseYn()).willReturn(true);
        given(image2.getUrl()).willReturn("image-url-2");
        PostImage postImage2 = mock(PostImage.class);
        given(postImage2.getPost()).willReturn(post);
        given(postImage2.getImage()).willReturn(image2);

        given(postImageRepository.findAllWithImageByPostIn(postSlice.getContent()))
                .willReturn(List.of(postImage1, postImage2));

        // when
        Slice<PostDto> result = postService.getList(PageRequest.of(0, 10), PostSearch.builder().build());

        // then
        List<PostDto> content = result.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting("id").containsExactly(1L);
        assertThat(content).extracting("content").containsExactly("content");
        assertThat(content).extracting("createdAt").containsExactly(LocalDateTime.of(2022, 11, 1, 1, 12));
        assertThat(content).extracting("commentCount").containsExactly(10);
        assertThat(content).extracting("likeCount").containsExactly(20);
        assertThat(content).extracting("member.id").containsExactly(2L);
        assertThat(content).extracting("member.nickname").containsExactly("nickname");
        assertThat(content).extracting("member.imageUrl").containsExactly((Object) null);
        assertThat(content).extracting("restaurant.id").containsExactly(3L);
        assertThat(content).extracting("restaurant.name").containsExactly("지그재그");

        assertThat(content.get(0).getImageUrls()).containsExactly("image-url-1", "image-url-2");
    }

    @Test
    void getList_DeletedPostImageContain() {
        // given
        Image memberImage = mock(Image.class);
        given(memberImage.getUseYn()).willReturn(true);
        given(memberImage.getUrl()).willReturn("member-image-url");

        Member member = mock(Member.class);
        given(member.getId()).willReturn(2L);
        given(member.getNickname()).willReturn("nickname");
        given(member.getImage()).willReturn(memberImage);

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(3L);
        given(restaurant.getName()).willReturn("지그재그");

        Post post = mock(Post.class);
        given(post.getId()).willReturn(1L);
        given(post.getContent()).willReturn("content");
        given(post.getCommentCount()).willReturn(10);
        given(post.getLikeCount()).willReturn(20);
        given(post.getCreatedAt()).willReturn(LocalDateTime.of(2022, 11, 1, 1, 12));
        given(post.getMember()).willReturn(member);
        given(post.getRestaurant()).willReturn(restaurant);

        SliceImpl<Post> postSlice = new SliceImpl<>(List.of(post));
        given(postRepository.findSliceWithMemberAndRestaurantBy(any(Pageable.class), any(PostSearch.class)))
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

        // when
        Slice<PostDto> result = postService.getList(PageRequest.of(0, 10), PostSearch.builder().build());

        // then
        List<PostDto> content = result.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting("id").containsExactly(1L);
        assertThat(content).extracting("content").containsExactly("content");
        assertThat(content).extracting("createdAt").containsExactly(LocalDateTime.of(2022, 11, 1, 1, 12));
        assertThat(content).extracting("commentCount").containsExactly(10);
        assertThat(content).extracting("likeCount").containsExactly(20);
        assertThat(content).extracting("member.id").containsExactly(2L);
        assertThat(content).extracting("member.nickname").containsExactly("nickname");
        assertThat(content).extracting("member.imageUrl").containsExactly("member-image-url");
        assertThat(content).extracting("restaurant.id").containsExactly(3L);
        assertThat(content).extracting("restaurant.name").containsExactly("지그재그");

        assertThat(content.get(0).getImageUrls()).containsExactly("image-url-1");
    }

    @Test
    void getListOfMember() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(2L)).willReturn(Optional.of(member));

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(3L);
        given(restaurant.getName()).willReturn("지그재그");

        Post post = mock(Post.class);
        given(post.getId()).willReturn(1L);
        given(post.getContent()).willReturn("content");
        given(post.getCommentCount()).willReturn(10);
        given(post.getLikeCount()).willReturn(20);
        given(post.getCreatedAt()).willReturn(LocalDateTime.of(2022, 11, 1, 2, 6));
        given(post.getRestaurant()).willReturn(restaurant);

        given(postRepository.findSliceWithRestaurantByMemberAndUseYnIsTrue(any(Pageable.class), eq(member)))
                .willReturn(new SliceImpl<>(List.of(post)));

        Image image1 = mock(Image.class);
        given(image1.getUseYn()).willReturn(true);
        given(image1.getUrl()).willReturn("image-url-1");
        PostImage postImage1 = mock(PostImage.class);
        given(postImage1.getPost()).willReturn(post);
        given(postImage1.getImage()).willReturn(image1);

        Image image2 = mock(Image.class);
        given(image2.getUseYn()).willReturn(true);
        given(image2.getUrl()).willReturn("image-url-2");
        PostImage postImage2 = mock(PostImage.class);
        given(postImage2.getPost()).willReturn(post);
        given(postImage2.getImage()).willReturn(image2);

        given(postImageRepository.findAllWithImageByPostIn(List.of(post)))
                .willReturn(List.of(postImage1, postImage2));

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<MemberPostDto> result = postService.getListOfMember(pageRequest, 2L);

        // then
        List<MemberPostDto> content = result.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting("id").containsExactly(1L);
        assertThat(content).extracting("content").containsExactly("content");
        assertThat(content).extracting("createdAt").containsExactly(LocalDateTime.of(2022, 11, 1, 2, 6));
        assertThat(content).extracting("commentCount").containsExactly(10);
        assertThat(content).extracting("likeCount").containsExactly(20);
        assertThat(content).extracting("restaurant.id").containsExactly(3L);
        assertThat(content).extracting("restaurant.name").containsExactly("지그재그");

        assertThat(content.get(0).getImageUrls()).containsExactly("image-url-1", "image-url-2");
    }

    @Test
    void getListOfMember_DeletedPostImageContain() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(2L)).willReturn(Optional.of(member));

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(3L);
        given(restaurant.getName()).willReturn("지그재그");

        Post post = mock(Post.class);
        given(post.getId()).willReturn(1L);
        given(post.getContent()).willReturn("content");
        given(post.getCommentCount()).willReturn(10);
        given(post.getLikeCount()).willReturn(20);
        given(post.getCreatedAt()).willReturn(LocalDateTime.of(2022, 11, 1, 2, 6));
        given(post.getRestaurant()).willReturn(restaurant);

        given(postRepository.findSliceWithRestaurantByMemberAndUseYnIsTrue(any(Pageable.class), eq(member)))
                .willReturn(new SliceImpl<>(List.of(post)));

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

        given(postImageRepository.findAllWithImageByPostIn(List.of(post)))
                .willReturn(List.of(postImage1, postImage2));

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<MemberPostDto> result = postService.getListOfMember(pageRequest, 2L);

        // then
        List<MemberPostDto> content = result.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting("id").containsExactly(1L);
        assertThat(content).extracting("content").containsExactly("content");
        assertThat(content).extracting("createdAt").containsExactly(LocalDateTime.of(2022, 11, 1, 2, 6));
        assertThat(content).extracting("commentCount").containsExactly(10);
        assertThat(content).extracting("likeCount").containsExactly(20);
        assertThat(content).extracting("restaurant.id").containsExactly(3L);
        assertThat(content).extracting("restaurant.name").containsExactly("지그재그");

        assertThat(content.get(0).getImageUrls()).containsExactly("image-url-1");
    }

    @Test
    void getListOfRestaurant() {
        // given
        Image memberImage = mock(Image.class);
        given(memberImage.getUseYn()).willReturn(true);
        given(memberImage.getUrl()).willReturn("member-image-url");

        Member member = mock(Member.class);
        given(member.getId()).willReturn(2L);
        given(member.getNickname()).willReturn("nickname");
        given(member.getImage()).willReturn(memberImage);

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findById(3L)).willReturn(Optional.of(restaurant));

        Post post = mock(Post.class);
        given(post.getId()).willReturn(1L);
        given(post.getContent()).willReturn("content");
        given(post.getCreatedAt()).willReturn(LocalDateTime.of(2022, 11, 1, 3, 25));
        given(post.getMember()).willReturn(member);

        given(postRepository.findSliceWithMemberByRestaurantAndUseYnIsTrue(any(Pageable.class), eq(restaurant)))
                .willReturn(new SliceImpl<>(List.of(post)));

        Image image1 = mock(Image.class);
        given(image1.getUseYn()).willReturn(true);
        given(image1.getUrl()).willReturn("image-url-1");
        PostImage postImage1 = mock(PostImage.class);
        given(postImage1.getPost()).willReturn(post);
        given(postImage1.getImage()).willReturn(image1);

        Image image2 = mock(Image.class);
        given(image2.getUseYn()).willReturn(true);
        given(image2.getUrl()).willReturn("image-url-2");
        PostImage postImage2 = mock(PostImage.class);
        given(postImage2.getPost()).willReturn(post);
        given(postImage2.getImage()).willReturn(image2);

        given(postImageRepository.findAllWithImageByPostIn(List.of(post)))
                .willReturn(List.of(postImage1, postImage2));

        // when
        Slice<RestaurantPostDto> result = postService.getListOfRestaurant(PageRequest.of(0, 10), 3L);

        // then
        List<RestaurantPostDto> content = result.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting("id").containsExactly(1L);
        assertThat(content).extracting("content").containsExactly("content");
        assertThat(content).extracting("createdAt").containsExactly(LocalDateTime.of(2022, 11, 1, 3, 25));
        assertThat(content).extracting("member.id").containsExactly(2L);
        assertThat(content).extracting("member.nickname").containsExactly("nickname");
        assertThat(content).extracting("member.imageUrl").containsExactly("member-image-url");

        assertThat(content.get(0).getImageUrls()).containsExactly("image-url-1", "image-url-2");
    }

    @Test
    void getListOfRestaurant_MemberImageNull() {
        // given
        Member member = mock(Member.class);
        given(member.getId()).willReturn(2L);
        given(member.getNickname()).willReturn("nickname");

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findById(3L)).willReturn(Optional.of(restaurant));

        Post post = mock(Post.class);
        given(post.getId()).willReturn(1L);
        given(post.getContent()).willReturn("content");
        given(post.getCreatedAt()).willReturn(LocalDateTime.of(2022, 11, 1, 3, 25));
        given(post.getMember()).willReturn(member);

        given(postRepository.findSliceWithMemberByRestaurantAndUseYnIsTrue(any(Pageable.class), eq(restaurant)))
                .willReturn(new SliceImpl<>(List.of(post)));

        Image image1 = mock(Image.class);
        given(image1.getUseYn()).willReturn(true);
        given(image1.getUrl()).willReturn("image-url-1");
        PostImage postImage1 = mock(PostImage.class);
        given(postImage1.getPost()).willReturn(post);
        given(postImage1.getImage()).willReturn(image1);

        Image image2 = mock(Image.class);
        given(image2.getUseYn()).willReturn(true);
        given(image2.getUrl()).willReturn("image-url-2");
        PostImage postImage2 = mock(PostImage.class);
        given(postImage2.getPost()).willReturn(post);
        given(postImage2.getImage()).willReturn(image2);

        given(postImageRepository.findAllWithImageByPostIn(List.of(post)))
                .willReturn(List.of(postImage1, postImage2));

        // when
        Slice<RestaurantPostDto> result = postService.getListOfRestaurant(PageRequest.of(0, 10), 3L);

        // then
        List<RestaurantPostDto> content = result.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting("id").containsExactly(1L);
        assertThat(content).extracting("content").containsExactly("content");
        assertThat(content).extracting("createdAt").containsExactly(LocalDateTime.of(2022, 11, 1, 3, 25));
        assertThat(content).extracting("member.id").containsExactly(2L);
        assertThat(content).extracting("member.nickname").containsExactly("nickname");
        assertThat(content).extracting("member.imageUrl").containsExactly((Object) null);

        assertThat(content.get(0).getImageUrls()).containsExactly("image-url-1", "image-url-2");
    }

    @Test
    void getListOfRestaurant_DeletedImageContain() {
        // given
        Image memberImage = mock(Image.class);
        given(memberImage.getUseYn()).willReturn(true);
        given(memberImage.getUrl()).willReturn("member-image-url");

        Member member = mock(Member.class);
        given(member.getId()).willReturn(2L);
        given(member.getNickname()).willReturn("nickname");
        given(member.getImage()).willReturn(memberImage);

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findById(3L)).willReturn(Optional.of(restaurant));

        Post post = mock(Post.class);
        given(post.getId()).willReturn(1L);
        given(post.getContent()).willReturn("content");
        given(post.getCreatedAt()).willReturn(LocalDateTime.of(2022, 11, 1, 3, 25));
        given(post.getMember()).willReturn(member);

        given(postRepository.findSliceWithMemberByRestaurantAndUseYnIsTrue(any(Pageable.class), eq(restaurant)))
                .willReturn(new SliceImpl<>(List.of(post)));

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

        given(postImageRepository.findAllWithImageByPostIn(List.of(post)))
                .willReturn(List.of(postImage1, postImage2));

        // when
        Slice<RestaurantPostDto> result = postService.getListOfRestaurant(PageRequest.of(0, 10), 3L);

        // then
        List<RestaurantPostDto> content = result.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting("id").containsExactly(1L);
        assertThat(content).extracting("content").containsExactly("content");
        assertThat(content).extracting("createdAt").containsExactly(LocalDateTime.of(2022, 11, 1, 3, 25));
        assertThat(content).extracting("member.id").containsExactly(2L);
        assertThat(content).extracting("member.nickname").containsExactly("nickname");
        assertThat(content).extracting("member.imageUrl").containsExactly("member-image-url");

        assertThat(content.get(0).getImageUrls()).containsExactly("image-url-1");
    }

    @Test
    void get() {
        // given
        Address address = mock(Address.class);
        given(address.getName()).willReturn("화양동");

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(3L);
        given(restaurant.getName()).willReturn("지그재그");
        given(restaurant.getAddress()).willReturn(address);

        Image memberImage = mock(Image.class);
        given(memberImage.getUseYn()).willReturn(true);
        given(memberImage.getUrl()).willReturn("member-image-url");

        Member member = mock(Member.class);
        given(member.getId()).willReturn(2L);
        given(member.getNickname()).willReturn("nickname");
        given(member.getImage()).willReturn(memberImage);

        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(post.getId()).willReturn(1L);
        given(post.getHashtags()).willReturn(List.of(Hashtag.STRONG_TASTE, Hashtag.CLEANLINESS));
        given(post.getContent()).willReturn("content");
        given(post.getCommentCount()).willReturn(10);
        given(post.getLikeCount()).willReturn(20);
        given(post.getCreatedAt()).willReturn(LocalDateTime.of(2022, 10, 31, 16, 36));
        given(post.getMember()).willReturn(member);
        given(post.getRestaurant()).willReturn(restaurant);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Image image1 = mock(Image.class);
        given(image1.getUseYn()).willReturn(true);
        given(image1.getUrl()).willReturn("image-url-1");
        PostImage postImage1 = mock(PostImage.class);
        given(postImage1.getImage()).willReturn(image1);

        Image image2 = mock(Image.class);
        given(image2.getUseYn()).willReturn(true);
        given(image2.getUrl()).willReturn("image-url-2");
        PostImage postImage2 = mock(PostImage.class);
        given(postImage2.getImage()).willReturn(image2);

        given(postImageRepository.findAllWithImageByPost(post)).willReturn(List.of(postImage1, postImage2));

        // when
        PostDetailDto result = postService.get(1L);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getImageUrls()).containsExactly("image-url-1", "image-url-2");
        assertThat(result.getContent()).isEqualTo("content");
        assertThat(result.getHashtags()).containsExactly(Hashtag.STRONG_TASTE, Hashtag.CLEANLINESS);
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2022, 10, 31, 16, 36));
        assertThat(result.getCommentCount()).isEqualTo(10);
        assertThat(result.getLikeCount()).isEqualTo(20);
        assertThat(result.getMember().getId()).isEqualTo(2L);
        assertThat(result.getMember().getNickname()).isEqualTo("nickname");
        assertThat(result.getMember().getImageUrl()).isEqualTo("member-image-url");
        assertThat(result.getRestaurant().getId()).isEqualTo(3L);
        assertThat(result.getRestaurant().getName()).isEqualTo("지그재그");
        assertThat(result.getRestaurant().getAddress().getName()).isEqualTo("화양동");
    }

    @Test
    void get_DeletedImageContain() {
        // given
        Address address = mock(Address.class);
        given(address.getName()).willReturn("화양동");

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(3L);
        given(restaurant.getName()).willReturn("지그재그");
        given(restaurant.getAddress()).willReturn(address);

        Image memberImage = mock(Image.class);
        given(memberImage.getUseYn()).willReturn(true);
        given(memberImage.getUrl()).willReturn("member-image-url");

        Member member = mock(Member.class);
        given(member.getId()).willReturn(2L);
        given(member.getNickname()).willReturn("nickname");
        given(member.getImage()).willReturn(memberImage);

        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(post.getId()).willReturn(1L);
        given(post.getHashtags()).willReturn(List.of(Hashtag.STRONG_TASTE, Hashtag.CLEANLINESS));
        given(post.getContent()).willReturn("content");
        given(post.getCommentCount()).willReturn(10);
        given(post.getLikeCount()).willReturn(20);
        given(post.getCreatedAt()).willReturn(LocalDateTime.of(2022, 10, 31, 16, 36));
        given(post.getMember()).willReturn(member);
        given(post.getRestaurant()).willReturn(restaurant);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Image image1 = mock(Image.class);
        given(image1.getUseYn()).willReturn(true);
        given(image1.getUrl()).willReturn("image-url-1");
        PostImage postImage1 = mock(PostImage.class);
        given(postImage1.getImage()).willReturn(image1);

        Image image2 = mock(Image.class);
        given(image2.getUseYn()).willReturn(false);
        PostImage postImage2 = mock(PostImage.class);
        given(postImage2.getImage()).willReturn(image2);

        given(postImageRepository.findAllWithImageByPost(post)).willReturn(List.of(postImage1, postImage2));

        // when
        PostDetailDto result = postService.get(1L);

        // then
        assertThat(result.getImageUrls()).containsExactly("image-url-1");
    }

    @Test
    void get_MemberImageNull() {
        // given
        Address address = mock(Address.class);
        given(address.getName()).willReturn("화양동");

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(3L);
        given(restaurant.getName()).willReturn("지그재그");
        given(restaurant.getAddress()).willReturn(address);

        Member member = mock(Member.class);
        given(member.getId()).willReturn(2L);
        given(member.getNickname()).willReturn("nickname");

        Post post = mock(Post.class);
        given(post.getUseYn()).willReturn(true);
        given(post.getId()).willReturn(1L);
        given(post.getHashtags()).willReturn(List.of(Hashtag.STRONG_TASTE, Hashtag.CLEANLINESS));
        given(post.getContent()).willReturn("content");
        given(post.getCommentCount()).willReturn(10);
        given(post.getLikeCount()).willReturn(20);
        given(post.getCreatedAt()).willReturn(LocalDateTime.of(2022, 10, 31, 16, 36));
        given(post.getMember()).willReturn(member);
        given(post.getRestaurant()).willReturn(restaurant);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Image image1 = mock(Image.class);
        given(image1.getUseYn()).willReturn(true);
        given(image1.getUrl()).willReturn("image-url-1");
        PostImage postImage1 = mock(PostImage.class);
        given(postImage1.getImage()).willReturn(image1);

        Image image2 = mock(Image.class);
        given(image2.getUseYn()).willReturn(true);
        given(image2.getUrl()).willReturn("image-url-2");
        PostImage postImage2 = mock(PostImage.class);
        given(postImage2.getImage()).willReturn(image2);

        given(postImageRepository.findAllWithImageByPost(post)).willReturn(List.of(postImage1, postImage2));

        // when
        PostDetailDto result = postService.get(1L);

        // then
        assertThat(result.getMember().getImageUrl()).isNull();
    }

    @Test
    void deleteAllOfMember() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        Restaurant restaurant1 = mock(Restaurant.class);
        given(restaurant1.getId()).willReturn(10L);
        Restaurant restaurant2 = mock(Restaurant.class);
        given(restaurant2.getId()).willReturn(20L);

        Post post1 = mock(Post.class);
        given(post1.getRestaurant()).willReturn(restaurant1);
        Post post2 = mock(Post.class);
        given(post2.getRestaurant()).willReturn(restaurant1);
        Post post3 = mock(Post.class);
        given(post3.getRestaurant()).willReturn(restaurant2);
        List<Post> posts = List.of(post1, post2, post3);
        given(postRepository.findAllByMemberAndUseYnIsTrue(member))
                .willReturn(posts);

        Restaurant lockedRestaurant1 = mock(Restaurant.class);
        given(lockedRestaurant1.getId()).willReturn(10L);
        Restaurant lockedRestaurant2 = mock(Restaurant.class);
        given(lockedRestaurant2.getId()).willReturn(20L);
        given(restaurantRepository.findAllWithPessimisticLockByIdIn(Set.of(10L, 20L)))
                .willReturn(List.of(lockedRestaurant1, lockedRestaurant2));

        // when
        postService.deleteAllOfMember(1L);

        // then
        then(commentRepository).should().updateUseYnFalseByPostIn(posts);
        then(post1).should().delete();
        then(post2).should().delete();
        then(post3).should().delete();

        then(lockedRestaurant1).should(times(2)).decreasePostCount();
        then(lockedRestaurant2).should(times(1)).decreasePostCount();
    }

}