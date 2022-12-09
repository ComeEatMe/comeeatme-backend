package com.comeeatme.domain.favorite.service;

import com.comeeatme.domain.favorite.Favorite;
import com.comeeatme.domain.favorite.repository.FavoriteRepository;
import com.comeeatme.domain.favorite.response.FavoriteRestaurantDto;
import com.comeeatme.domain.favorite.response.RestaurantFavorited;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import com.comeeatme.error.exception.AlreadyFavoriteException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @InjectMocks
    private FavoriteService favoriteService;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MemberRepository memberRepository;

    @Test
    void favorite_GroupNull() {
        // given
        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findWithPessimisticLockById(1L)).willReturn(Optional.of(restaurant));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(2L)).willReturn(Optional.of(member));

        given(favoriteRepository.existsByRestaurantAndMember(restaurant, member)).willReturn(false);

        // when
        favoriteService.favorite(1L, 2L);

        // then
        ArgumentCaptor<Favorite> favoriteCaptor = ArgumentCaptor.forClass(Favorite.class);
        then(favoriteRepository).should().save(favoriteCaptor.capture());

        Favorite captorValue = favoriteCaptor.getValue();
        assertThat(captorValue.getMember()).isEqualTo(member);
        assertThat(captorValue.getRestaurant()).isEqualTo(restaurant);

        then(restaurant).should().increaseFavoriteCount();
    }

    @Test
    void favorite_AlreadyFavorite() {
        // given
        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findWithPessimisticLockById(1L)).willReturn(Optional.of(restaurant));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(2L)).willReturn(Optional.of(member));

        given(favoriteRepository.existsByRestaurantAndMember(restaurant, member)).willReturn(true);

        // expected
        assertThatThrownBy(() -> favoriteService.favorite(1L, 2L))
                .isInstanceOf(AlreadyFavoriteException.class);
    }

    @Test
    void cancelFavorite() {
        // given
        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findWithPessimisticLockById(1L)).willReturn(Optional.of(restaurant));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(2L)).willReturn(Optional.of(member));

        Favorite favorite = mock(Favorite.class);
        given(favoriteRepository.findByRestaurantAndMember(restaurant, member))
                .willReturn(Optional.of(favorite));

        // when
        favoriteService.cancelFavorite(1L, 2L);

        // then
        then(favoriteRepository).should().delete(favorite);
        then(restaurant).should().decreaseFavoriteCount();
    }

    @Test
    void areFavorite() {
        // given
        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(1L);
        Favorite favorite = mock(Favorite.class);
        given(favorite.getRestaurant()).willReturn(restaurant);

        given(favoriteRepository.findAllByMemberIdAndRestaurantIds(3L, List.of(1L, 2L)))
                .willReturn(List.of(favorite));

        // when
        List<RestaurantFavorited> result = favoriteService.areFavorite(3L, List.of(1L, 2L));

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("restaurantId").containsExactly(1L, 2L);
        assertThat(result).extracting("favorited").containsExactly(true, false);
    }

    @Test
    void getFavoriteRestaurants() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(2L);
        given(restaurant.getName()).willReturn("지그재그");

        Favorite favorite = mock(Favorite.class);
        given(favorite.getRestaurant()).willReturn(restaurant);

        given(favoriteRepository.findSliceWithRestaurantByMember(any(Pageable.class), eq(member)))
                .willReturn(new SliceImpl<>(List.of(favorite)));

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<FavoriteRestaurantDto> result = favoriteService.getFavoriteRestaurants(pageRequest, 1L);

        //then
        List<FavoriteRestaurantDto> content = result.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting("id").containsExactly(2L);
        assertThat(content).extracting("name").containsExactly("지그재그");
    }

    @Test
    void isFavorite_True() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findById(2L)).willReturn(Optional.of(restaurant));

        given(favoriteRepository.existsByRestaurantAndMember(restaurant, member)).willReturn(true);

        // expected
        assertThat(favoriteService.isFavorite(1L, 2L)).isTrue();
    }

    @Test
    void isFavorite_False() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findById(2L)).willReturn(Optional.of(restaurant));

        given(favoriteRepository.existsByRestaurantAndMember(restaurant, member)).willReturn(false);

        // expected
        assertThat(favoriteService.isFavorite(1L, 2L)).isFalse();
    }

}