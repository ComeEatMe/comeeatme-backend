package com.comeeatme.domain.favorite.service;

import com.comeeatme.domain.favorite.Favorite;
import com.comeeatme.domain.favorite.FavoriteGroup;
import com.comeeatme.domain.favorite.repository.FavoriteGroupRepository;
import com.comeeatme.domain.favorite.repository.FavoriteRepository;
import com.comeeatme.domain.member.Member;
import com.comeeatme.domain.member.repository.MemberRepository;
import com.comeeatme.domain.restaurant.Restaurant;
import com.comeeatme.domain.restaurant.repository.RestaurantRepository;
import com.comeeatme.error.exception.AlreadyFavoriteException;
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteGroupRepository favoriteGroupRepository;

    private final FavoriteRepository favoriteRepository;

    private final RestaurantRepository restaurantRepository;

    private final MemberRepository memberRepository;

    @Transactional
    public void favorite(Long restaurantId, String username, @Nullable String groupName) {
        Restaurant restaurant = getRestaurantById(restaurantId);
        Member member = getMemberByUsername(username);
        FavoriteGroup group = Optional.ofNullable(groupName)
                .map(name -> getFavoriteGroupByMemberAndName(member, name))
                .orElse(null);
        if (favoriteRepository.existsByGroupAndRestaurant(group, restaurant)) {
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
        Optional.ofNullable(group)
                .ifPresent(FavoriteGroup::incrFavoriteCount);
    }

    private Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .filter(Restaurant::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant.id=" + id));
    }

    private Member getMemberByUsername(String username) {
        return memberRepository.findByUsername(username)
                .filter(Member::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("Member username=" + username));
    }

    private FavoriteGroup getFavoriteGroupByMemberAndName(Member member, String name) {
        return favoriteGroupRepository.findByMemberAndName(member, name)
                .orElseThrow(() -> new EntityNotFoundException(String.format(
                        "FavoriteGroup member.id=%s, name=%s", member.getId(), name)
                ));
    }

}
