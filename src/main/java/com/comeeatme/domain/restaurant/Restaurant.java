package com.comeeatme.domain.restaurant;

import com.comeeatme.domain.common.core.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.persistence.*;

@Entity
@Table(name = "restaurant",
        indexes = {
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

    @Column(name = "name", length = 100, nullable = false, updatable = false)
    private String name;

    @Column(name = "phone", length = 25, nullable = false, updatable = false)
    private String phone;

    @Embedded
    private Address address;

    @Builder
    private Restaurant(
            @Nullable Long id,
            String name,
            String phone,
            Address address) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
    }
}
