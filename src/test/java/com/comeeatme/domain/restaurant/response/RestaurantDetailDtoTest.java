package com.comeeatme.domain.restaurant.response;

import com.comeeatme.domain.address.Address;
import com.comeeatme.domain.restaurant.OpenInfo;
import com.comeeatme.domain.restaurant.Restaurant;
import org.junit.jupiter.api.Test;
import org.springframework.data.geo.Point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class RestaurantDetailDtoTest {

    @Test
    void of() {
        // given
        Address address = mock(Address.class);
        given(address.getName()).willReturn("소재지주소");
        given(address.getRoadName()).willReturn("도로명주소");
        given(address.getPoint()).willReturn(new Point(1.0, 2.0));

        OpenInfo openInfo = mock(OpenInfo.class);
        given(openInfo.getCategory()).willReturn("한식");

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(3L);
        given(restaurant.getName()).willReturn("음식점");
        given(restaurant.getAddress()).willReturn(address);
        given(restaurant.getOpenInfo()).willReturn(openInfo);

        // when
        RestaurantDetailDto result = RestaurantDetailDto.of(restaurant);

        // then
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("음식점");
        assertThat(result.getCategory()).isEqualTo("한식");
        assertThat(result.getAddress().getName()).isEqualTo("소재지주소");
        assertThat(result.getAddress().getRoadName()).isEqualTo("도로명주소");
        assertThat(result.getAddress().getX()).isEqualTo(1.0);
        assertThat(result.getAddress().getY()).isEqualTo(2.0);
    }

    @Test
    void of_NullOpenInfo() {
        // given
        Address address = mock(Address.class);
        given(address.getName()).willReturn("소재지주소");
        given(address.getRoadName()).willReturn("도로명주소");
        given(address.getPoint()).willReturn(new Point(1.0, 2.0));

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(3L);
        given(restaurant.getName()).willReturn("음식점");
        given(restaurant.getAddress()).willReturn(address);

        // when
        RestaurantDetailDto result = RestaurantDetailDto.of(restaurant);

        // then
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("음식점");
        assertThat(result.getCategory()).isNull();
        assertThat(result.getAddress().getName()).isEqualTo("소재지주소");
        assertThat(result.getAddress().getRoadName()).isEqualTo("도로명주소");
        assertThat(result.getAddress().getX()).isEqualTo(1.0);
        assertThat(result.getAddress().getY()).isEqualTo(2.0);
    }

}