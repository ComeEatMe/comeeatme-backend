package com.comeeatme.domain.member.request;

import com.comeeatme.domain.member.MemberDeleteReason;
import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberDelete {

    @NotNull
    private MemberDeleteReason reason;
}
