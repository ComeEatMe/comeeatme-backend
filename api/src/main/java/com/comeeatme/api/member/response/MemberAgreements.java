package com.comeeatme.api.member.response;

import com.comeeatme.domain.member.Agreement;
import lombok.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberAgreements {

    private List<AgreementDto> agreements;

    public static MemberAgreements create() {
        return new MemberAgreements(
                Arrays.stream(Agreement.values())
                        .map(AgreementDto::of)
                        .collect(Collectors.toList())
        );
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AgreementDto {
        private String code;
        private String title;
        private Boolean required;
        private String link;

        public static AgreementDto of(Agreement agreement) {
            return AgreementDto.builder()
                    .code(agreement.name())
                    .title(agreement.getTitle())
                    .required(agreement.isRequired())
                    .link(agreement.getLink())
                    .build();
        }
    }
}
