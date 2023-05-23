package com.comeeatme.web.v1;

import com.comeeatme.api.notice.NoticeService;
import com.comeeatme.api.notice.response.NoticeDto;
import com.comeeatme.web.common.response.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/notices")
    public ResponseEntity<ApiResult<Slice<NoticeDto>>> getList(Pageable pageable) {
        Slice<NoticeDto> notices = noticeService.getList(pageable);
        ApiResult<Slice<NoticeDto>> apiResult = ApiResult.success(notices);
        return ResponseEntity.ok(apiResult);
    }

}
