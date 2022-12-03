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
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteGroupRepository favoriteGroupRepository;

    private final FavoriteRepository favoriteRepository;

    private final RestaurantRepository restaurantRepository;

    private final MemberRepository memberRepository;

    @Transactional
    public void favorite(Long restaurantId, Long memberId, @Nullable String groupName) {
        Restaurant restaurant = getRestaurantWithPessimisticLockById(restaurantId);
        Member member = getMemberById(memberId);
        FavoriteGroup group = Optional.ofNullable(groupName)
                .map(name -> getFavoriteGroupByMemberAndName(member, name))
                .orElse(null);
        if (favoriteRepository.existsByMemberAndGroupAndRestaurant(member, group, restaurant)) {
            throw new AlreadyFavoriteException(String.format(
                    "member.id=%s, favorite.group=%s, restaurant.id=%s",
                    member.getId(), groupName, restaurant.getId()
            ));
        }

        favoriteRepository.save(Favorite.builder()
                .member(member)
                .group(group)
                .restaurant(restaurant)
                .build());
        restaurant.increaseFavoriteCount();
        Optional.ofNullable(group)
                .ifPresent(FavoriteGroup::incrFavoriteCount);
    }

    @Transactional
    public void cancelFavorite(Long restaurantId, Long memberId, String groupName) {
        Restaurant restaurant = getRestaurantWithPessimisticLockById(restaurantId);
        Member member = getMemberById(memberId);
        FavoriteGroup group = Optional.ofNullable(groupName)
                .map(name -> getFavoriteGroupByMemberAndName(member, name))
                .orElse(null);
        Favorite favorite = favoriteRepository.findByMemberAndGroupAndRestaurant(member, group, restaurant)
                .orElseThrow(() -> new EntityNotFoundException(
                        "group=" + groupName + ", restaurant.id" + restaurantId));

        favoriteRepository.delete(favorite);
        restaurant.decreaseFavoriteCount();
        Optional.ofNullable(group)
                .ifPresent(FavoriteGroup::decrFavoriteCount);
    }

    public List<FavoriteGroupDto> getAllGroupsOfMember(Long memberId) {
        Member member = getMemberById(memberId);
        List<FavoriteGroup> groups = favoriteGroupRepository.findAllByMember(member);
        int allCount = (int) favoriteRepository.countByMember(member);
        List<FavoriteGroupDto> groupDtos = new ArrayList<>();
        groupDtos.add(FavoriteGroupDto.builder()
                .name(FavoriteGroup.ALL_NAME)
                .favoriteCount(allCount)
                .build());
        groups.stream()
                .map(group -> FavoriteGroupDto.builder()
                        .name(group.getName())
                        .favoriteCount(group.getFavoriteCount())
                        .build())
                .forEach(groupDtos::add);
        return groupDtos;
    }

    public List<RestaurantFavorited> areFavorite(Long memberId, List<Long> restaurantIds) {
        List<Favorite> favorites = favoriteRepository.findAllByMemberIdAndRestaurantIds(memberId, restaurantIds);
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
        return favoriteRepository.existsByMemberAndRestaurant(member, restaurant);
    }

    public Slice<FavoriteRestaurantDto> getFavoriteRestaurants(
            Pageable pageable, Long memberId, @Nullable String groupName) {
        Member member = getMemberById(memberId);
        FavoriteGroup group = Optional.ofNullable(groupName)
                .map(name -> getFavoriteGroupByMemberAndName(member, name))
                .orElse(null);
        Slice<Restaurant> favoriteRestaurants = favoriteRepository.findSliceWithByMemberAndGroup(
                        pageable, member, group)
                .map(Favorite::getRestaurant);
        return favoriteRestaurants
                .map(restaurant -> FavoriteRestaurantDto.builder()
                        .id(restaurant.getId())
                        .name(restaurant.getName())
                        .build()
                );
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

    private FavoriteGroup getFavoriteGroupByMemberAndName(Member member, String name) {
        return favoriteGroupRepository.findByMemberAndName(member, name)
                .orElseThrow(() -> new EntityNotFoundException(String.format(
                        "FavoriteGroup member.id=%s, name=%s", member.getId(), name)
                ));
    }

}
