package com.comeeatme.domain.member.request;

import com.comeeatme.domain.member.Agreement;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberSignup {

    @NotNull
    private Map<Agreement, Boolean> agreeOrNot;
}
