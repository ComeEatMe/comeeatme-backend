package com.comeeatme.domain.bookmark.repository;

import com.comeeatme.domain.bookmark.Bookmark;
import com.comeeatme.domain.post.Post;

import java.util.List;

public interface BookmarkRepositoryCustom {

    List<Bookmark> findByMemberIdAndPostIds(Long memberId, List<Long> postIds);

    void deleteAllByPost(Post post);

}
