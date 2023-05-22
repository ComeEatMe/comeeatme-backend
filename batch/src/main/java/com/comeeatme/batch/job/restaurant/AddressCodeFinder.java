package com.comeeatme.batch.job.restaurant;

import com.comeeatme.batch.job.restaurant.exception.NotFoundAddressCodeException;
import com.comeeatme.domain.address.AddressCode;
import com.comeeatme.domain.address.repository.AddressCodeRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@StepScope
@Component
public class AddressCodeFinder {

    private final Map<String, AddressCode> addressToCode;

    public AddressCodeFinder(AddressCodeRepository addressCodeRepository) {
        this.addressToCode = addressCodeRepository.findAllByTerminalIsTrueAndUseYnIsTrue()
                .stream()
                .collect(Collectors.toMap(AddressCode::getFullName, Function.identity()));
    }

    public AddressCode findByFullAddress(String address) {
        StringBuilder sb = new StringBuilder();
        for (String addressToken : address.split("[\\s,]+")) {
            sb.append(addressToken);
            String subAddress = sb.toString();
            if (addressToCode.containsKey(subAddress)) {
                return addressToCode.get(subAddress);
            }
            sb.append(" ");
        }
        throw new NotFoundAddressCodeException(address);
    }
}
