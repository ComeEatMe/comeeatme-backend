package com.comeeatme.domain.address;

import com.comeeatme.domain.core.BaseUseYnEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.Optional;

/**
 * 국토교통부 전국 법정동 : https://www.data.go.kr/data/15063424/fileData.do#tab-layer-file
 */
@Entity
@Table(name = "address_code",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_address_code_code", columnNames = "code"),
                @UniqueConstraint(name = "UK_address_code_full_name", columnNames = "full_name")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AddressCode extends BaseUseYnEntity {

    public static final int SIDO_CODE_LEN = 2;
    public static final int SIGUNGU_CODE_LEN = 3;
    public static final int EUPMYEONDONG_CODE_LEN = 3;
    public static final int RI_CODE_LEN = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_code_id")
    private Long id;

    @Column(name = "code", length = 15, nullable = false, updatable = false)
    private String code;

    @Column(name = "si_do", length = 15, nullable = false, updatable = false)
    private String siDo;

    @Column(name = "si_gun_gu", length = 15, updatable = false)
    private String siGunGu;

    @Column(name = "eup_myeon_dong", length = 15, updatable = false)
    private String eupMyeonDong;

    @Column(name = "ri", length = 15, updatable = false)
    private String ri;

    @Column(name = "full_name", length = 45, nullable = false, updatable = false)
    private String fullName;

    @Builder
    private AddressCode(
            @Nullable Long id,
            String code,
            String siDo,
            @Nullable String siGunGu,
            @Nullable String eupMyeonDong,
            @Nullable String ri) {
        this.id = id;
        this.code = code;
        this.siDo = siDo;
        this.siGunGu = siGunGu;
        this.eupMyeonDong = eupMyeonDong;
        this.ri = ri;
        StringBuilder sb = new StringBuilder();
        Optional.ofNullable(this.siDo).ifPresent(sb::append);
        Optional.ofNullable(this.siGunGu).ifPresent(sb::append);
        Optional.ofNullable(this.eupMyeonDong).ifPresent(sb::append);
        Optional.ofNullable(this.ri).ifPresent(sb::append);
        this.fullName = sb.toString();
    }
}
