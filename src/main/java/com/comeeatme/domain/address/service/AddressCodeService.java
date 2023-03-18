package com.comeeatme.domain.address.service;

import com.comeeatme.domain.address.AddressCode;
import com.comeeatme.domain.address.repository.AddressCodeRepository;
import com.comeeatme.domain.address.response.AddressCodeDto;
import com.comeeatme.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AddressCodeService {

    private final AddressCodeRepository addressCodeRepository;

    public List<AddressCodeDto> getListOfParentCode(@Nullable String parentCode) {
        AddressCode parent = Optional.ofNullable(parentCode)
                .map(this::getAddressCodeByCode)
                .orElse(null);
        List<AddressCode> addressCodes = addressCodeRepository.findAllByParentCodeAndUseYnIsTrue(parent);
        return addressCodes.stream()
                .map(addressCode -> AddressCodeDto.builder()
                        .code(addressCode.getCode())
                        .name(addressCode.getName())
                        .terminal(addressCode.getTerminal())
                        .build()
                ).collect(Collectors.toList());
    }

    private AddressCode getAddressCodeByCode(String code) {
        return addressCodeRepository.findById(code)
                .filter(AddressCode::getUseYn)
                .orElseThrow(() -> new EntityNotFoundException("AddressCode.code=" + code));
    }

}
