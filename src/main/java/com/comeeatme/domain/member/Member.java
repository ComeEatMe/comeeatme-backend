package com.comeeatme.domain.member;

import com.comeeatme.domain.common.core.BaseTimeEntity;
import com.comeeatme.domain.image.Image;
import lombok.*;

import javax.annotation.Nullable;
import javax.persistence.*;

@Entity
@Table(name = "member",
        uniqueConstraints = @UniqueConstraint(name = "UK_member_nickname", columnNames = "nickname"),
        indexes = @Index(name = "IX_member_image", columnList = "image_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "nickname", length = 25, nullable = false)
    private String nickname;

    @Column(name = "introduction", length = 100, nullable = false)
    private String introduction;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Image image;

    @Enumerated(EnumType.STRING)
    @Column(name = "delete_reason", length = 65)
    @Setter
    private MemberDeleteReason deleteReason;

    @Builder
    private Member(
            String nickname,
            String introduction,
            @Nullable Image image) {
        this.nickname = nickname;
        this.introduction = introduction;
        this.image = image;
    }

    public MemberEditor.MemberEditorBuilder toEditor() {
        return MemberEditor.builder()
                .nickname(nickname)
                .introduction(introduction)
                .image(image);
    }

    public void edit(MemberEditor editor) {
        nickname = editor.getNickname();
        introduction = editor.getIntroduction();
        image = editor.getImage();
    }
}
