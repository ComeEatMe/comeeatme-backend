package com.comeeatme.domain.notice;

import com.comeeatme.domain.common.core.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "notice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 25, nullable = false)
    private NoticeType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", length = 4000, nullable = false)
    private String content;

    @Builder
    private Notice(NoticeType type, String title, String content) {
        this.type = type;
        this.title = title;
        this.content = content;
    }

}
