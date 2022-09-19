package com.comeeatme.domain.restaurant;

import com.comeeatme.domain.core.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.persistence.*;

@Entity
@Table(name = "restaurant", uniqueConstraints =
        @UniqueConstraint(name = "UK_restaurant_kakao_restaurant_id", columnNames = "kakao_restaurant_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Restaurant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_id")
    private Long id;

    @Column(name = "kakao_restaurant_id", nullable = false)
    private Long kakaoId;

    @Column(name = "kakao_place_url", nullable = false)
    private String kakaoPlaceUrl;

    @Column(name = "name", length = 45, nullable = false)
    private String name;

    @Column(name = "category_name", length = 45, nullable = false)
    private String categoryName;

    @Column(name = "phone", length = 25, nullable = false)
    private String phone;

    @Embedded
    private Address address;

    @Builder
    private Restaurant(
            @Nullable Long id,
            Long kakaoId,
            String kakaoPlaceUrl,
            String name,
            String categoryName,
            String phone,
            Address address) {
        this.id = id;
        this.kakaoId = kakaoId;
        this.kakaoPlaceUrl = kakaoPlaceUrl;
        this.name = name;
        this.categoryName = categoryName;
        this.phone = phone;
        this.address = address;
    }
}
