package com.comeeatme.domain.restaurant.service;

import com.comeeatme.domain.address.Address;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import com.comeeatme.domain.restaurant.response.RestaurantSimpleDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @InjectMocks
    private RestaurantService restaurantService;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Test
    void getSimpleList() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);

        Address address1 = mock(Address.class);
        given(address1.getName()).willReturn("야탑동");
        Restaurant restaurant1 = mock(Restaurant.class);
        given(restaurant1.getId()).willReturn(1L);
        given(restaurant1.getName()).willReturn("음식점1");
        given(restaurant1.getAddress()).willReturn(address1);

        Address address2 = mock(Address.class);
        given(address2.getName()).willReturn("이매동" );
        Restaurant restaurant2 = mock(Restaurant.class);
        given(restaurant2.getId()).willReturn(2L);
        given(restaurant2.getName()).willReturn("음식점2");
        given(restaurant2.getAddress()).willReturn(address2);

        SliceImpl<Restaurant> slice = new SliceImpl<>(List.of(restaurant1, restaurant2), pageRequest, false);
        given(restaurantRepository.findSliceByNameStartingWithAndUseYnIsTrue(any(Pageable.class), anyString())).willReturn(slice);

        // when
        Slice<RestaurantSimpleDto> simpleDtos = restaurantService.getSimpleList(pageRequest, "음식점");

        // then
        List<RestaurantSimpleDto> content = simpleDtos.getContent();
        assertThat(content).hasSize(2);
        assertThat(content).extracting("id").containsExactly(1L, 2L);
        assertThat(content).extracting("name").containsExactly("음식점1", "음식점2");
        assertThat(content).extracting("addressName").containsExactly("야탑동", "이매동");
    }
}