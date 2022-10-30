package com.comeeatme.domain.favorite.service;

import com.comeeatme.domain.favorite.Favorite;
import com.comeeatme.domain.favorite.FavoriteGroup;
import com.comeeatme.domain.favorite.repository.FavoriteGroupRepository;
import com.comeeatme.domain.favorite.repository.FavoriteRepository;
import com.comeeatme.domain.favorite.response.FavoriteGroupDto;
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
    private FavoriteGroupRepository favoriteGroupRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MemberRepository memberRepository;

    @Test
    void favorite_GroupNotNull() {
        // given
        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findById(1L)).willReturn(Optional.of(restaurant));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findByUsername("username")).willReturn(Optional.of(member));

        FavoriteGroup group = mock(FavoriteGroup.class);
        given(favoriteGroupRepository.findByMemberAndName(member, "그루비룸")).willReturn(Optional.of(group));

        given(favoriteRepository.existsByGroupAndRestaurant(group, restaurant)).willReturn(false);

        // when
        favoriteService.favorite(1L, "username", "그루비룸");

        // then
        ArgumentCaptor<Favorite> favoriteCaptor = ArgumentCaptor.forClass(Favorite.class);
        then(favoriteRepository).should().save(favoriteCaptor.capture());

        Favorite captorValue = favoriteCaptor.getValue();
        assertThat(captorValue.getMember()).isEqualTo(member);
        assertThat(captorValue.getGroup()).isEqualTo(group);
        assertThat(captorValue.getRestaurant()).isEqualTo(restaurant);

        then(group).should().incrFavoriteCount();
    }

    @Test
    void favorite_GroupNull() {
        // given
        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findById(1L)).willReturn(Optional.of(restaurant));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findByUsername("username")).willReturn(Optional.of(member));

        given(favoriteRepository.existsByGroupAndRestaurant(null, restaurant)).willReturn(false);

        // when
        favoriteService.favorite(1L, "username", null);

        // then
        ArgumentCaptor<Favorite> favoriteCaptor = ArgumentCaptor.forClass(Favorite.class);
        then(favoriteRepository).should().save(favoriteCaptor.capture());

        Favorite captorValue = favoriteCaptor.getValue();
        assertThat(captorValue.getMember()).isEqualTo(member);
        assertThat(captorValue.getGroup()).isNull();
        assertThat(captorValue.getRestaurant()).isEqualTo(restaurant);
    }

    @Test
    void favorite_AlreadyFavorite() {
        // given
        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findById(1L)).willReturn(Optional.of(restaurant));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findByUsername("username")).willReturn(Optional.of(member));

        FavoriteGroup group = mock(FavoriteGroup.class);
        given(favoriteGroupRepository.findByMemberAndName(member, "그루비룸")).willReturn(Optional.of(group));

        given(favoriteRepository.existsByGroupAndRestaurant(group, restaurant)).willReturn(true);

        // expected
        assertThatThrownBy(() -> favoriteService.favorite(1L, "username", "그루비룸"))
                .isInstanceOf(AlreadyFavoriteException.class);
    }

    @Test
    void cancelFavorite() {
        // given
        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findById(1L)).willReturn(Optional.of(restaurant));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findByUsername("username")).willReturn(Optional.of(member));

        FavoriteGroup group = mock(FavoriteGroup.class);
        given(favoriteGroupRepository.findByMemberAndName(member, "그루비룸"))
                .willReturn(Optional.of(group));

        Favorite favorite = mock(Favorite.class);
        given(favoriteRepository.findByGroupAndRestaurant(group, restaurant))
                .willReturn(Optional.of(favorite));

        // when
        favoriteService.cancelFavorite(1L, "username", "그루비룸");

        // then
        then(favoriteRepository).should().delete(favorite);
        then(group).should().decrFavoriteCount();
    }

    @Test
    void cancelFavorite_GroupNull() {
        // given
        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getUseYn()).willReturn(true);
        given(restaurantRepository.findById(1L)).willReturn(Optional.of(restaurant));

        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findByUsername("username")).willReturn(Optional.of(member));

        Favorite favorite = mock(Favorite.class);
        given(favoriteRepository.findByGroupAndRestaurant(null, restaurant))
                .willReturn(Optional.of(favorite));

        // when
        favoriteService.cancelFavorite(1L, "username", null);

        // then
        then(favoriteRepository).should().delete(favorite);
    }

    @Test
    void getAllGroupsOfMember() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        FavoriteGroup group1 = mock(FavoriteGroup.class);
        given(group1.getName()).willReturn("그루비룸-1");
        given(group1.getFavoriteCount()).willReturn(2);

        FavoriteGroup group2 = mock(FavoriteGroup.class);
        given(group2.getName()).willReturn("그루비룸-2");
        given(group2.getFavoriteCount()).willReturn(3);

        given(favoriteGroupRepository.findAllByMember(member)).willReturn(List.of(group1, group2));

        given(favoriteRepository.countByMember(member)).willReturn(10);

        // when
        List<FavoriteGroupDto> result = favoriteService.getAllGroupsOfMember(1L);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting("name")
                .containsExactly(FavoriteGroup.ALL_NAME, "그루비룸-1", "그루비룸-2");
        assertThat(result).extracting("favoriteCount")
                .containsExactly(10, 2, 3);
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

        FavoriteGroup group = mock(FavoriteGroup.class);
        given(favoriteGroupRepository.findByMemberAndName(member, "그루비룸")).willReturn(Optional.of(group));

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(2L);
        given(restaurant.getName()).willReturn("지그재그");

        Favorite favorite = mock(Favorite.class);
        given(favorite.getRestaurant()).willReturn(restaurant);

        given(favoriteRepository.findSliceWithByMemberAndGroup(any(Pageable.class), eq(member), eq(group)))
                .willReturn(new SliceImpl<>(List.of(favorite)));

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<FavoriteRestaurantDto> result = favoriteService.getFavoriteRestaurants(pageRequest, 1L, "그루비룸");

        //then
        List<FavoriteRestaurantDto> content = result.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting("id").containsExactly(2L);
        assertThat(content).extracting("name").containsExactly("지그재그");
    }

    @Test
    void getFavoriteRestaurants_GroupNull() {
        // given
        Member member = mock(Member.class);
        given(member.getUseYn()).willReturn(true);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(2L);
        given(restaurant.getName()).willReturn("지그재그");

        Favorite favorite = mock(Favorite.class);
        given(favorite.getRestaurant()).willReturn(restaurant);

        given(favoriteRepository.findSliceWithByMemberAndGroup(any(Pageable.class), eq(member), eq(null)))
                .willReturn(new SliceImpl<>(List.of(favorite)));

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<FavoriteRestaurantDto> result = favoriteService.getFavoriteRestaurants(pageRequest, 1L, null);

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

        given(favoriteRepository.existsByMemberAndRestaurant(member, restaurant)).willReturn(true);

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

        given(favoriteRepository.existsByMemberAndRestaurant(member, restaurant)).willReturn(false);

        // expected
        assertThat(favoriteService.isFavorite(1L, 2L)).isFalse();
    }

}