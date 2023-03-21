package com.comeeatme.api.like.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LikedPostDto {

    private Long id;

    private List<String> imageUrls;

    private String content;

    private LocalDateTime createdAt;
}
