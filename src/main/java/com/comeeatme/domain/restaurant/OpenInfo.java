package com.comeeatme.domain.restaurant;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenInfo {

    // 개방서비스아이디
    @Column(name = "open_id", length = 15)
    private String id;

    // 개방서비스명
    @Column(name = "open_name", length = 15)
    private String name;

    // 업태구분명
    @Column(name = "open_category", length = 15)
    private String category;

    // 관리번호
    @Column(name = "open_management_num", length = 65)
    private String managementNum;

    // 인허가일자
    @Column(name = "open_permission_date")
    private LocalDate permissionDate;

    // 최종수정일자
    @Column(name = "open_last_modified_at")
    private LocalDateTime lastModifiedAt;

    @Builder
    private OpenInfo(
            String id,
            String name,
            String category,
            String managementNum,
            LocalDate permissionDate,
            LocalDateTime lastModifiedAt) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.managementNum = managementNum;
        this.permissionDate = permissionDate;
        this.lastModifiedAt = lastModifiedAt;
    }
}
