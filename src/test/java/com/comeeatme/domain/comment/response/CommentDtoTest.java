package com.comeeatme.domain.comment.response;

import com.comeeatme.domain.comment.Comment;
import com.comeeatme.domain.images.Images;
import com.comeeatme.domain.member.Member;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class CommentDtoTest {

    @Test
    void of_Undeleted_NotContainsNull() {
        Comment parent = mock(Comment.class);
        given(parent.getId()).willReturn(2L);
        Images image = mock(Images.class);
        given(image.getUrl()).willReturn("image-url");
        Member member = mock(Member.class);
        given(member.getId()).willReturn(3L);
        given(member.getNickname()).willReturn("nickname");
        given(member.getImage()).willReturn(image);
        Comment comment = mock(Comment.class);
        given(comment.getId()).willReturn(4L);
        given(comment.getParent()).willReturn(parent);
        given(comment.getContent()).willReturn("content");
        given(comment.getCreatedAt()).willReturn(LocalDateTime.of(2022, 3, 1, 12, 30));
        given(comment.getMember()).willReturn(member);
        given(comment.getUseYn()).willReturn(true);

        // when
        CommentDto commentDto = CommentDto.of(comment);

        // then
        assertThat(commentDto.getId()).isEqualTo(4L);
        assertThat(commentDto.getParentId()).isEqualTo(2L);
        assertThat(commentDto.getDeleted()).isFalse();
        assertThat(commentDto.getCreatedAt()).isEqualTo(LocalDateTime.of(2022, 3, 1, 12, 30));
        assertThat(commentDto.getContent()).isEqualTo("content");
        assertThat(commentDto.getMember().getId()).isEqualTo(3L);
        assertThat(commentDto.getMember().getNickname()).isEqualTo("nickname");
        assertThat(commentDto.getMember().getImageUrl()).isEqualTo("image-url");
    }

    @Test
    void of_Undeleted_ContainsNull() {
        Member member = mock(Member.class);
        given(member.getId()).willReturn(3L);
        given(member.getNickname()).willReturn("nickname");
        given(member.getImage()).willReturn(null);
        Comment comment = mock(Comment.class);
        given(comment.getId()).willReturn(4L);
        given(comment.getParent()).willReturn(null);
        given(comment.getContent()).willReturn("content");
        given(comment.getCreatedAt()).willReturn(LocalDateTime.of(2022, 3, 1, 12, 30));
        given(comment.getMember()).willReturn(member);
        given(comment.getUseYn()).willReturn(true);

        // when
        CommentDto commentDto = CommentDto.of(comment);

        // then
        assertThat(commentDto.getId()).isEqualTo(4L);
        assertThat(commentDto.getParentId()).isNull();
        assertThat(commentDto.getDeleted()).isFalse();
        assertThat(commentDto.getCreatedAt()).isEqualTo(LocalDateTime.of(2022, 3, 1, 12, 30));
        assertThat(commentDto.getContent()).isEqualTo("content");
        assertThat(commentDto.getMember().getId()).isEqualTo(3L);
        assertThat(commentDto.getMember().getNickname()).isEqualTo("nickname");
        assertThat(commentDto.getMember().getImageUrl()).isNull();
    }

    @Test
    void of_Deleted() {
        Comment parent = mock(Comment.class);
        given(parent.getId()).willReturn(2L);
        Images image = mock(Images.class);
        given(image.getUrl()).willReturn("image-url");
        Member member = mock(Member.class);
        given(member.getId()).willReturn(3L);
        given(member.getNickname()).willReturn("nickname");
        given(member.getImage()).willReturn(image);
        Comment comment = mock(Comment.class);
        given(comment.getId()).willReturn(4L);
        given(comment.getParent()).willReturn(parent);
        given(comment.getContent()).willReturn("content");
        given(comment.getCreatedAt()).willReturn(LocalDateTime.of(2022, 3, 1, 12, 30));
        given(comment.getMember()).willReturn(member);
        given(comment.getUseYn()).willReturn(false);

        // when
        CommentDto commentDto = CommentDto.of(comment);

        // then
        assertThat(commentDto.getId()).isEqualTo(4L);
        assertThat(commentDto.getParentId()).isEqualTo(2L);
        assertThat(commentDto.getDeleted()).isTrue();
        assertThat(commentDto.getCreatedAt()).isNull();
        assertThat(commentDto.getContent()).isNull();
        assertThat(commentDto.getMember().getId()).isNull();
        assertThat(commentDto.getMember().getNickname()).isNull();
        assertThat(commentDto.getMember().getImageUrl()).isNull();
    }
}