package com.comeeatme.api.v1.restaurants;

import com.comeeatme.api.common.dto.ApiResult;
import com.comeeatme.domain.restaurant.response.RestaurantSimpleDto;
import com.comeeatme.domain.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/v1/restaurants")
@RestController
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping("/simple")
    public ResponseEntity<ApiResult<Slice<RestaurantSimpleDto>>> getSimpleList(
            Pageable pageable, @RequestParam String name) {
        Slice<RestaurantSimpleDto> simpleList = restaurantService.getSimpleList(pageable, name);
        ApiResult<Slice<RestaurantSimpleDto>> result = ApiResult.success(simpleList);
        return ResponseEntity.ok(result);
    }
}
