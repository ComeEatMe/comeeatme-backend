package com.comeeatme.domain.comment.repository;

import com.comeeatme.domain.comment.Comment;
import com.comeeatme.domain.post.Post;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static com.comeeatme.domain.comment.QComment.comment;
import static com.comeeatme.domain.image.QImage.image;
import static com.comeeatme.domain.member.QMember.member;

@RequiredArgsConstructor
public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public Slice<Comment> findSliceByPostWithMemberAndImage(Pageable pageable, Post post) {
        List<Comment> content = query
                .selectFrom(comment)
                .join(comment.post)
                .join(comment.member, member).fetchJoin()
                .leftJoin(member.image, image).fetchJoin()
                .where(comment.post.eq(post))
                .orderBy(comment.parent.id.coalesce(comment.id).asc(), comment.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1L)
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public void updateUseYnFalseByPostIn(List<Post> posts) {
        query
                .update(comment)
                .set(comment.useYn, false)
                .where(comment.post.in(posts))
                .execute();
    }

}
