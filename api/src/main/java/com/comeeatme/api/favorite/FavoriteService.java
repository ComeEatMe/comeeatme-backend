package com.comeeatme.api.favorite;

import com.comeeatme.api.exception.AlreadyFavoriteException;
import com.comeeatme.api.exception.EntityNotFoundException;
import com.comeeatme.api.favorite.response.FavoriteRestaurantDto;
import com.comeeatme.api.favorite.response.RestaurantFavorited;
import com.comeeatme.domain.favorite.Favorite;
import com.comeeatme.domain.favorite.repository.FavoriteRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    private final RestaurantRepository restaurantRepository;

    private final MemberRepository memberRepository;

    @Transactional
    public void favorite(Long restaurantId, Long memberId) {
        Restaurant restaurant = getRestaurantWithPessimisticLockById(restaurantId);
        Member member = getMemberById(memberId);
        if (favoriteRepository.existsByRestaurantAndMember(restaurant, member)) {
            throw new AlreadyFavoriteException(String.format(
                    "member.id=%s, restaurant.id=%s", member.getId(), restaurant.getId()
            ));
        }

        favoriteRepository.save(Favorite.builder()
                .member(member)
                .restaurant(restaurant)
                .build());
        restaurant.increaseFavoriteCount();
    }

    @Transactional
    public void cancelFavorite(Long restaurantId, Long memberId) {
        Restaurant restaurant = getRestaurantWithPessimisticLockById(restaurantId);
        Member member = getMemberById(memberId);
        Favorite favorite = favoriteRepository.findByRestaurantAndMember(restaurant, member)
                .orElseThrow(() -> new EntityNotFoundException(String.format(
                        "memberId=%s, restaurantId=%s", memberId, restaurantId
                )));

        favoriteRepository.delete(favorite);
        restaurant.decreaseFavoriteCount();
    }

    public List<RestaurantFavorited> areFavorite(Long memberId, List<Long> restaurantIds) {
        Member member = memberRepository.getReferenceById(memberId);
        List<Restaurant> restaurants = restaurantIds.stream()
                .map(restaurantRepository::getReferenceById)
                .collect(Collectors.toList());
        List<Favorite> favorites = favoriteRepository.findAllByMemberAndRestaurantIn(member, restaurants);
        Set<Long> favoriteRestaurantIds = favorites.stream()
                .map(favorite -> favorite.getRestaurant().getId())
                .collect(Collectors.toSet());
        return restaurantIds.stream()
                .map(restaurantId -> RestaurantFavorited.builder()
                        .restaurantId(restaurantId)
                        .favorited(favoriteRestaurantIds.contains(restaurantId))
                        .build()
                ).collect(Collectors.toList());
    }

    public boolean isFavorite(Long memberId, Long restaurantId) {
        Member member = getMemberById(memberId);
        Restaurant restaurant = getRestaurantById(restaurantId);
        return favoriteRepository.existsByRestaurantAndMember(restaurant, member);
    }

    public Slice<FavoriteRestaurantDto> getFavoriteRestaurants(Pageable pageable, Long memberId) {
        Member member = getMemberById(memberId);
        Slice<Restaurant> favoriteRestaurants = favoriteRepository.findSliceWithRestaurantByMember(
                        pageable, member)
                .map(Favorite::getRestaurant);
        return favoriteRestaurants
                .map(restaurant -> FavoriteRestaurantDto.builder()
                        .id(restaurant.getId())
                        .name(restaurant.getName())
                        .build()
                );
    }

    @Transactional
    public void deleteAllOfMember(Long memberId) {
        Member member = getMemberById(memberId);
        List<Favorite> favorites = favoriteRepository.findAllByMember(member);
        favoriteRepository.deleteAll(favorites);
        List<Long> restaurantIds = favorites.stream()
                .map(favorite -> favorite.getRestaurant().getId())
                .collect(Collectors.toList());
        List<Restaurant> restaurants = restaurantRepository.findAllWithPessimisticLockByIdIn(restaurantIds);
        restaurants.forEach(Restaurant::decreaseFavoriteCount);
    }

    private Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .filter(Restaurant::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant.id=" + id));
    }

    private Restaurant getRestaurantWithPessimisticLockById(Long restaurantId) {
        return restaurantRepository.findWithPessimisticLockById(restaurantId)
                .filter(Restaurant::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant id=" + restaurantId));
    }

    private Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .filter(Member::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Member.id=" + id));
    }

}
