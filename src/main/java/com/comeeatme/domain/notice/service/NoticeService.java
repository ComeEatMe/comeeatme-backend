package com.comeeatme.domain.notice.service;

import com.comeeatme.domain.notice.repository.NoticeRepository;
import com.comeeatme.domain.notice.response.NoticeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public Slice<NoticeDto> getList(Pageable pageable) {
        return noticeRepository.findSliceByUseYnIsTrue(pageable)
                .map(notice -> NoticeDto.builder()
                        .type(notice.getType())
                        .title(notice.getTitle())
                        .content(notice.getContent())
                        .createdAt(notice.getCreatedAt())
                        .build()
                );
    }
}
