package com.comeeatme.api.v1;

import com.comeeatme.domain.like.service.LikeService;
import com.comeeatme.security.annotation.CurrentUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PutMapping("/member/like/{postId}")
    public ResponseEntity<Void> like(@PathVariable Long postId, @CurrentUsername String username) {
        likeService.like(postId, username);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/member/like/{postId}")
    public ResponseEntity<Void> unlike(@PathVariable Long postId, @CurrentUsername String username) {
        likeService.unlike(postId, username);
        return ResponseEntity.noContent().build();
    }

}
