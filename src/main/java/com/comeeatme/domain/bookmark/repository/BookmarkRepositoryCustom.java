package com.comeeatme.domain.bookmark.repository;

import com.comeeatme.domain.post.Post;

public interface BookmarkRepositoryCustom {

    void deleteAllByPost(Post post);

}
