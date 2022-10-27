package com.comeeatme.domain.post.response;

import com.comeeatme.domain.comment.response.CommentCount;
import com.comeeatme.domain.images.Images;
import com.comeeatme.domain.like.response.LikeCount;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.post.PostImage;
import com.comeeatme.domain.restaurant.Restaurant;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class PostDtoTest {

    @Test
    void of() {
        // given
        Images image = mock(Images.class);
        given(image.getUseYn()).willReturn(true);
        given(image.getUrl()).willReturn("image-url");

        PostImage postImage = mock(PostImage.class);
        given(postImage.getUseYn()).willReturn(true);
        given(postImage.getImage()).willReturn(image);

        Images memberImage = mock(Images.class);
        given(memberImage.getUseYn()).willReturn(true);
        given(memberImage.getUrl()).willReturn("member-image-url");

        Member member = mock(Member.class);
        given(member.getId()).willReturn(2L);
        given(member.getNickname()).willReturn("nickname");
        given(member.getImage()).willReturn(memberImage);

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(3L);
        given(restaurant.getName()).willReturn("restaurant");

        Post post = mock(Post.class);
        given(post.getId()).willReturn(1L);
        given(post.getContent()).willReturn("content");
        given(post.getCreatedAt()).willReturn(LocalDateTime.of(2022, 10, 9, 15, 50));
        given(post.getMember()).willReturn(member);
        given(post.getRestaurant()).willReturn(restaurant);

        CommentCount commentCount = mock(CommentCount.class);
        given(commentCount.getCount()).willReturn(10L);

        LikeCount likeCount = mock(LikeCount.class);
        given(likeCount.getCount()).willReturn(20L);

        // when
        PostDto postDto = PostDto.of(post, List.of(postImage), commentCount, likeCount);

        // then
        assertThat(postDto.getId()).isEqualTo(1L);
        assertThat(postDto.getImageUrls()).containsExactly("image-url");
        assertThat(postDto.getContent()).isEqualTo("content");
        assertThat(postDto.getCreatedAt()).isEqualTo(LocalDateTime.of(2022, 10, 9, 15, 50));
        assertThat(postDto.getCommentCount()).isEqualTo(10L);
        assertThat(postDto.getLikeCount()).isEqualTo(20L);
        assertThat(postDto.getMember().getId()).isEqualTo(2L);
        assertThat(postDto.getMember().getNickname()).isEqualTo("nickname");
        assertThat(postDto.getMember().getImageUrl()).isEqualTo("member-image-url");
        assertThat(post.getRestaurant().getId()).isEqualTo(3L);
        assertThat(post.getRestaurant().getName()).isEqualTo("restaurant");
    }
}