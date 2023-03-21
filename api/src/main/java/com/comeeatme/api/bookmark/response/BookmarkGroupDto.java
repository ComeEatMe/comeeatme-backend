package com.comeeatme.api.bookmark.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BookmarkGroupDto {

    private String name;

    private Integer bookmarkCount;
}
