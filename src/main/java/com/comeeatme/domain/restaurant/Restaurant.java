package com.comeeatme.domain.restaurant;

import com.comeeatme.domain.address.Address;
import com.comeeatme.domain.core.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.persistence.*;

@Entity
@Table(name = "restaurant",
        uniqueConstraints = {
        @UniqueConstraint(name = "UK_restaurant_open_info_id", columnNames = "open_info_id")
        }, indexes = {
        @Index(name = "IX_restaurant_name", columnList = "name")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Restaurant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_id")
    private Long id;

    @Column(name = "name", length = 45, nullable = false)
    private String name;

    @Column(name = "phone", length = 25, nullable = false)
    private String phone;

    @Embedded
    private Address address;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "open_info_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private OpenInfo openInfo;

    @Builder
    private Restaurant(
            @Nullable Long id,
            String name,
            String phone,
            Address address,
            @Nullable OpenInfo openInfo) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.openInfo = openInfo;
    }
}
