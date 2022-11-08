package com.comeeatme.domain.restaurant;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "open_info",
        uniqueConstraints = @UniqueConstraint(name = "UK_open_info_management_num", columnNames = "management_num"),
        indexes = @Index(name = "IX_openinfo_restaurant", columnList = "restaurant_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "open_info_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), updatable = false)
    private Restaurant restaurant;

    // 관리번호
    @Column(name = "management_num", length = 65, nullable = false, updatable = false)
    private String managementNum;

    // 개방서비스아이디
    @Column(name = "service_id", length = 15, nullable = false, updatable = false)
    private String serviceId;

    // 개방서비스명
    @Column(name = "name", length = 15, nullable = false, updatable = false)
    private String name;

    // 업태구분명
    @Column(name = "category", length = 15, nullable = false, updatable = false)
    private String category;

    // 인허가일자
    @Column(name = "permission_date", nullable = false, updatable = false)
    private LocalDate permissionDate;

    // 최종수정시점
    @Column(name = "last_modified_at", nullable = false, updatable = false)
    private LocalDateTime lastModifiedAt;

    @Builder
    private OpenInfo(
            @Nullable Long id,
            Restaurant restaurant,
            String serviceId,
            String name,
            @Nullable String category,
            String managementNum,
            LocalDate permissionDate,
            LocalDateTime lastModifiedAt) {
        this.id = id;
        this.restaurant = restaurant;
        this.serviceId = serviceId;
        this.name = name;
        this.category = category;
        this.managementNum = managementNum;
        this.permissionDate = permissionDate;
        this.lastModifiedAt = lastModifiedAt;
    }

}
