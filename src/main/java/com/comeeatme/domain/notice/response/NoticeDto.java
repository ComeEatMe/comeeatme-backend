package com.comeeatme.domain.notice.response;

import com.comeeatme.domain.notice.NoticeType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NoticeDto {

    private NoticeType type;

    private String title;

    private String content;

    private LocalDateTime createdAt;

    @Builder
    private NoticeDto(NoticeType type, String title, String content, LocalDateTime createdAt) {
        this.type = type;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }
}
