package com.comeeatme.domain.report.request;

import com.comeeatme.domain.report.ReportReason;
import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportCreate {

    @NotNull
    private ReportReason reason;
}
