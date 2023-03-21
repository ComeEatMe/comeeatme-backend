package com.comeeatme.web.code.controller;

import com.comeeatme.api.address.AddressCodeService;
import com.comeeatme.api.address.response.AddressCodeDto;
import com.comeeatme.web.code.config.EnumMapperFactory;
import com.comeeatme.web.code.dto.EnumMapperValue;
import com.comeeatme.web.common.response.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

@RestController
@RequiredArgsConstructor
public class CodeController {

    private final EnumMapperFactory enumMapperFactory;

    private final AddressCodeService addressCodeService;

    @GetMapping("/code")
    public ResponseEntity<Map<String, List<EnumMapperValue>>> codeList(
            @RequestParam(required = false) List<String> codeTypes) {
        if (isNull(codeTypes)) {
            return ResponseEntity.ok(enumMapperFactory.getAll());
        }
        return ResponseEntity.ok(enumMapperFactory.get(codeTypes));
    }

    @GetMapping({"/code/address/{parentCode}", "/code/address"})
    public ResponseEntity<ApiResult<List<AddressCodeDto>>> getAddressCodeList(
            @PathVariable(required = false) String parentCode) {
        List<AddressCodeDto> addressCodes = addressCodeService.getListOfParentCode(parentCode);
        ApiResult<List<AddressCodeDto>> result = ApiResult.success(addressCodes);
        return ResponseEntity.ok(result);
    }
}
