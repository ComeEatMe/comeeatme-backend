package com.comeeatme.domain.bookmark.repository;

import com.comeeatme.domain.bookmark.Bookmark;
import com.comeeatme.domain.post.Post;
import com.comeeatme.domain.bookmark.BookmarkGroup;
import com.comeeatme.domain.member.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface BookmarkRepositoryCustom {

    List<Bookmark> findByMemberIdAndPostIds(Long memberId, List<Long> postIds);

    void deleteAllByPost(Post post);

    Slice<Bookmark> findSliceWithByMemberAndGroup(Pageable pageable, Member member, BookmarkGroup group);

}
