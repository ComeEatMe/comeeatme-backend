package com.comeeatme.security.account;

import com.comeeatme.domain.common.core.BaseTimeEntity;
import com.comeeatme.domain.member.Member;
import lombok.*;
import org.springframework.util.Assert;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.time.LocalDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    @Column(name = "username", nullable = false)
    private String username;

    /**
     * OAuth2 로그인의 경우 password 는 null 로 일반적인 로그인 불가능
     */
    @Column(name = "password")
    private String password;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    @Column(name = "refresh_token_expires_at", nullable = false)
    private LocalDateTime refreshTokenExpiresAt;

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
        this.refreshToken = "";
        this.refreshTokenExpiresAt = LocalDateTime.now();
    }

    public void renewRefreshToken(String refreshToken, LocalDateTime expiresAt) {
        Assert.notNull(Account.this.refreshToken, "refreshToken 은 null 일 수 없습니다.");
        Assert.notNull(expiresAt, "expiresAt 은 null 일 수 없습니다.");
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = expiresAt;
    }

    public void refreshTokenExpires() {
        this.refreshToken = "";
        this.refreshTokenExpiresAt = LocalDateTime.now();
    }

    public void setMember(Member member) {
        Assert.isNull(this.member, "회원이 이미 설정된 상태입니다.");

        Assert.notNull(member, "member 는 null 일 수 없습니다.");
        Assert.notNull(member.getId(), "member.id 는 null 일 수 없습니다.");

        this.member = member;
    }

}
