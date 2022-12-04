package com.comeeatme.domain.restaurant.service;

import com.comeeatme.domain.address.Address;
import com.comeeatme.domain.address.AddressCode;
import com.comeeatme.domain.address.repository.AddressCodeRepository;
import com.comeeatme.domain.favorite.repository.FavoriteRepository;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import com.comeeatme.domain.restaurant.request.RestaurantSearch;
import com.comeeatme.domain.restaurant.response.RestaurantDetailDto;
import com.comeeatme.domain.restaurant.response.RestaurantDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @InjectMocks
    private RestaurantService restaurantService;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private PostRepository postRepository;

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
        given(restaurant.getFavoriteCount()).willReturn(10);
        given(restaurant.getAddress()).willReturn(address);

        given(restaurantRepository.findById(1L)).willReturn(Optional.of(restaurant));

        // when
        RestaurantDetailDto result = restaurantService.get(1L);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("음식점");
        assertThat(result.getFavoriteCount()).isEqualTo(10);
        assertThat(result.getAddress().getName()).isEqualTo("소재지주소");
        assertThat(result.getAddress().getRoadName()).isEqualTo("도로명주소");
    }

}