package com.comeeatme.api.address;

import com.comeeatme.api.address.response.AddressCodeDto;
import com.comeeatme.domain.address.AddressCode;
import com.comeeatme.domain.address.repository.AddressCodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AddressCodeServiceTest {

    @InjectMocks
    private AddressCodeService addressCodeService;

    @Mock
    private AddressCodeRepository addressCodeRepository;

    @Test
    void getListOfParentCode_parentCodeNull() {
        // given
        AddressCode addressCode1 = mock(AddressCode.class);
        given(addressCode1.getCode()).willReturn("001");
        given(addressCode1.getName()).willReturn("주소1");
        given(addressCode1.getTerminal()).willReturn(true);

        AddressCode addressCode2 = mock(AddressCode.class);
        given(addressCode2.getCode()).willReturn("002");
        given(addressCode2.getName()).willReturn("주소2");
        given(addressCode2.getTerminal()).willReturn(true);

        given(addressCodeRepository.findAllByParentCodeAndUseYnIsTrue(null))
                .willReturn(List.of(addressCode1, addressCode2));

        // when
        List<AddressCodeDto> result = addressCodeService.getListOfParentCode(null);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("code").containsOnly("001", "002");
        assertThat(result).extracting("name").containsOnly("주소1", "주소2");
        assertThat(result).extracting("terminal").containsOnly(true, true);
    }
}