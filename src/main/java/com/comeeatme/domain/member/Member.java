package com.comeeatme.domain.member;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.persistence.*;

@Entity
@Table(name = "member",
        uniqueConstraints = @UniqueConstraint(name = "UK_member_nickname", columnNames = "nickname")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "nickname", length = 25, nullable = false)
    private String nickname;

    @Column(name = "introduction", length = 100, nullable = false)
    private String introduction;

    @Builder
    private Member(
            @Nullable Long id,
            String nickname,
            String introduction) {
        this.id = id;
        this.nickname = nickname;
        this.introduction = introduction;
    }
}
