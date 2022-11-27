package com.comeeatme.domain.restaurant.service;

import com.comeeatme.domain.favorite.repository.FavoriteRepository;
import com.comeeatme.domain.favorite.response.FavoriteCount;
import com.comeeatme.domain.post.Hashtag;
import com.comeeatme.domain.post.repository.PostRepository;
import com.comeeatme.domain.address.Address;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import com.comeeatme.domain.restaurant.request.RestaurantSearch;
import com.comeeatme.domain.restaurant.response.RestaurantDetailDto;
import com.comeeatme.domain.restaurant.response.RestaurantDto;
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

    @Test
    void getList() {
        // given
        Address address = mock(Address.class);
        given(address.getName()).willReturn("address-name");
        given(address.getRoadName()).willReturn("address-road-name");

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(1L);
        given(restaurant.getName()).willReturn("지그재그");
        given(restaurant.getAddress()).willReturn(address);

        given(restaurantRepository
                .findSliceByNameStartingWithAndUseYnIsTrue(any(Pageable.class), eq("지그재그")))
                .willReturn(new SliceImpl<>(List.of(restaurant)));

        given(favoriteRepository.countsGroupByRestaurants(List.of(restaurant)))
                .willReturn(List.of(new FavoriteCount(1L, 10L)));

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        RestaurantSearch restaurantSearch = RestaurantSearch.builder()
                .name("지그재그")
                .build();
        Slice<RestaurantDto> result = restaurantService.getList(pageRequest, restaurantSearch);

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
        given(restaurant.getAddress()).willReturn(address);

        given(restaurantRepository.findById(1L)).willReturn(Optional.of(restaurant));

        given(favoriteRepository.countByRestaurant(restaurant)).willReturn(10L);

        given(postRepository.findAllHashtagByRestaurant(restaurant))
                .willReturn(List.of(Hashtag.STRONG_TASTE, Hashtag.COST_EFFECTIVENESS));

        // when
        RestaurantDetailDto result = restaurantService.get(1L);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("음식점");
        assertThat(result.getFavoriteCount()).isEqualTo(10);
        assertThat(result.getHashtags()).containsOnly(Hashtag.STRONG_TASTE, Hashtag.COST_EFFECTIVENESS);
        assertThat(result.getAddress().getName()).isEqualTo("소재지주소");
        assertThat(result.getAddress().getRoadName()).isEqualTo("도로명주소");
    }

}