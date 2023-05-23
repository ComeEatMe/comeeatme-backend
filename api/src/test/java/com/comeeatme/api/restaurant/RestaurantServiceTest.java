package com.comeeatme.api.restaurant;

import com.comeeatme.api.restaurant.request.RestaurantSearch;
import com.comeeatme.api.restaurant.response.RestaurantDto;
import com.comeeatme.domain.address.Address;
import com.comeeatme.domain.address.AddressCode;
import com.comeeatme.domain.address.repository.AddressCodeRepository;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @InjectMocks
    private RestaurantService restaurantService;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private AddressCodeRepository addressCodeRepository;

    @Test
    void search() {
        // given
        Address address = mock(Address.class);
        given(address.getName()).willReturn("address-name");
        given(address.getRoadName()).willReturn("address-road-name");

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(1L);
        given(restaurant.getName()).willReturn("지그재그");
        given(restaurant.getPostCount()).willReturn(5);
        given(restaurant.getFavoriteCount()).willReturn(10);
        given(restaurant.getAddress()).willReturn(address);

        given(addressCodeRepository.findAllByNameStartingWith(anyString())).willReturn(Collections.emptyList());

        given(restaurantRepository
                .findSliceByNameAddressCodesStartingWithAndUseYnIsTrue(any(Pageable.class), eq("지그재그"), eq(null)))
                .willReturn(new SliceImpl<>(List.of(restaurant)));

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        RestaurantSearch restaurantSearch = RestaurantSearch.builder()
                .keyword("지그재그")
                .build();
        Slice<RestaurantDto> result = restaurantService.search(pageRequest, restaurantSearch);

        // then
        assertThat(result).hasSize(1);

        RestaurantDto dto = result.getContent().get(0);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("지그재그");
        assertThat(dto.getPostCount()).isEqualTo(5);
        assertThat(dto.getFavoriteCount()).isEqualTo(10);
        assertThat(dto.getAddress().getName()).isEqualTo("address-name");
        assertThat(dto.getAddress().getRoadName()).isEqualTo("address-road-name");
    }

    @Test
    void search_AddressContaining() {
        // given
        Address address = mock(Address.class);
        given(address.getName()).willReturn("address-name");
        given(address.getRoadName()).willReturn("address-road-name");

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(1L);
        given(restaurant.getName()).willReturn("지그재그");
        given(restaurant.getPostCount()).willReturn(5);
        given(restaurant.getFavoriteCount()).willReturn(10);
        given(restaurant.getAddress()).willReturn(address);

        AddressCode addressCode = mock(AddressCode.class);
        given(addressCode.getUseYn()).willReturn(true);
        given(addressCode.getCodePrefix()).willReturn("11215107");
        given(addressCodeRepository.findAllByNameStartingWith("화양")).willReturn(List.of(addressCode));

        given(restaurantRepository
                .findSliceByNameAddressCodesStartingWithAndUseYnIsTrue(
                        any(Pageable.class), eq("지그재그"), eq(List.of("11215107"))))
                .willReturn(new SliceImpl<>(List.of(restaurant)));

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        RestaurantSearch restaurantSearch = RestaurantSearch.builder()
                .keyword("화양 지그재그")
                .build();
        Slice<RestaurantDto> result = restaurantService.search(pageRequest, restaurantSearch);

        // then
        assertThat(result).hasSize(1);

        RestaurantDto dto = result.getContent().get(0);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("지그재그");
        assertThat(dto.getPostCount()).isEqualTo(5);
        assertThat(dto.getFavoriteCount()).isEqualTo(10);
        assertThat(dto.getAddress().getName()).isEqualTo("address-name");
        assertThat(dto.getAddress().getRoadName()).isEqualTo("address-road-name");
    }

    @Test
    void get() {
        // given
        Address address = mock(Address.class);
        given(address.getName()).willReturn("소재지주소");
        given(address.getRoadName()).willReturn("도로명주소");

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getUseYn()).willReturn(true);
        given(restaurant.getId()).willReturn(1L);
        given(restaurant.getName()).willReturn("음식점");
        given(restaurant.getPostCount()).willReturn(5);
        given(restaurant.getFavoriteCount()).willReturn(10);
        given(restaurant.getAddress()).willReturn(address);

        given(restaurantRepository.findById(1L)).willReturn(Optional.of(restaurant));

        // when
        RestaurantDto result = restaurantService.get(1L);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("음식점");
        assertThat(result.getPostCount()).isEqualTo(5);
        assertThat(result.getFavoriteCount()).isEqualTo(10);
        assertThat(result.getAddress().getName()).isEqualTo("소재지주소");
        assertThat(result.getAddress().getRoadName()).isEqualTo("도로명주소");
    }

    @Test
    void getOrderedList_AddressCodeNotNull() {
        // given
        AddressCode addressCode = mock(AddressCode.class);
        given(addressCode.getUseYn()).willReturn(true);
        given(addressCode.getCodePrefix()).willReturn("12345");
        given(addressCodeRepository.findById("1234567890")).willReturn(Optional.of(addressCode));

        Address address = mock(Address.class);
        given(address.getName()).willReturn("address-name");
        given(address.getRoadName()).willReturn("address-road-name");

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(1L);
        given(restaurant.getName()).willReturn("지그재그");
        given(restaurant.getPostCount()).willReturn(5);
        given(restaurant.getFavoriteCount()).willReturn(10);
        given(restaurant.getAddress()).willReturn(address);

        given(restaurantRepository
                .findSliceByAddressAddressCodeCodeStartingWithAndPostCountGreaterThanAndUseYnIsTrue(
                        any(Pageable.class), eq("12345"), eq(0)
                )).willReturn(new SliceImpl<>(List.of(restaurant)));

        // when
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "postCount");
        Slice<RestaurantDto> result = restaurantService.getOrderedList(pageRequest, "1234567890");

        // then
        List<RestaurantDto> content = result.getContent();
        assertThat(result).hasSize(1);

        RestaurantDto dto = content.get(0);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("지그재그");
        assertThat(dto.getPostCount()).isEqualTo(5);
        assertThat(dto.getFavoriteCount()).isEqualTo(10);
        assertThat(dto.getAddress().getName()).isEqualTo("address-name");
        assertThat(dto.getAddress().getRoadName()).isEqualTo("address-road-name");

        then(restaurantRepository).should(never())
                .findSliceByPostCountGreaterThanAndUseYnIsTrue(any(Pageable.class), anyInt());
    }

    @Test
    void getOrderedList_AddressCodeNull() {
        // given
        Address address = mock(Address.class);
        given(address.getName()).willReturn("address-name");
        given(address.getRoadName()).willReturn("address-road-name");

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(1L);
        given(restaurant.getName()).willReturn("지그재그");
        given(restaurant.getPostCount()).willReturn(5);
        given(restaurant.getFavoriteCount()).willReturn(10);
        given(restaurant.getAddress()).willReturn(address);

        given(restaurantRepository
                .findSliceByPostCountGreaterThanAndUseYnIsTrue(any(Pageable.class), eq(0)
                )).willReturn(new SliceImpl<>(List.of(restaurant)));

        // when
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "postCount");
        Slice<RestaurantDto> result = restaurantService.getOrderedList(pageRequest, null);

        // then
        List<RestaurantDto> content = result.getContent();
        assertThat(result).hasSize(1);

        RestaurantDto dto = content.get(0);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("지그재그");
        assertThat(dto.getPostCount()).isEqualTo(5);
        assertThat(dto.getFavoriteCount()).isEqualTo(10);
        assertThat(dto.getAddress().getName()).isEqualTo("address-name");
        assertThat(dto.getAddress().getRoadName()).isEqualTo("address-road-name");

        then(restaurantRepository).should(never())
                .findSliceByAddressAddressCodeCodeStartingWithAndPostCountGreaterThanAndUseYnIsTrue(
                        any(Pageable.class), anyString(), anyInt());
    }


}