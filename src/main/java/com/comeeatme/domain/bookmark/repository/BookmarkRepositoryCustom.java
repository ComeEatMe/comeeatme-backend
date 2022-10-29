package com.comeeatme.domain.bookmark.repository;

import com.comeeatme.domain.bookmark.Bookmark;

import java.util.List;

public interface BookmarkRepositoryCustom {

    List<Bookmark> findByMemberIdAndPostIds(Long memberId, List<Long> postIds);

}
