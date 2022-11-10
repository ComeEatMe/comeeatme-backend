package com.comeeatme.domain.account;

import com.comeeatme.domain.common.core.BaseTimeEntity;
import com.comeeatme.domain.member.Member;
import lombok.*;

import javax.annotation.Nullable;
import javax.persistence.*;

@Entity
@Table(name = "account",
        uniqueConstraints = @UniqueConstraint(name = "UK_account_username", columnNames = "username"),
        indexes = @Index(name = "IX_account_member", columnList = "member_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    @Column(name = "username", nullable = false)
    private String username;

    /**
     * OAuth2 로그인의 경우 password 는 null 로 일반적인 로그인 불가능
     */
    @Column(name = "password")
    private String password;

    @Setter
    @Column(name = "refresh_token")
    private String refreshToken;

    @Builder
    private Account(
            @Nullable Long id,
            String username,
            @Nullable String password,
            Member member) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.member = member;
    }
}
