package com.comeeatme.api.v1;

import com.comeeatme.domain.bookmark.service.BookmarkService;
import com.comeeatme.security.annotation.CurrentUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PutMapping({"/member/bookmark/{groupName}/{postId}", "/member/bookmark/{postId}"})
    public ResponseEntity<Void> bookmark(
            @PathVariable(required = false) String groupName, @PathVariable Long postId,
            @CurrentUsername String username) {
        bookmarkService.bookmark(postId, username, groupName);
        return ResponseEntity.noContent().build();
    }
}
