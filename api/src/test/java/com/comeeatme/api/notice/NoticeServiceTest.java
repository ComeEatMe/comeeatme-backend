package com.comeeatme.api.notice;

import com.comeeatme.api.notice.response.NoticeDto;
import com.comeeatme.domain.notice.Notice;
import com.comeeatme.domain.notice.NoticeType;
import com.comeeatme.domain.notice.repository.NoticeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @InjectMocks
    private NoticeService noticeService;

    @Mock
    private NoticeRepository noticeRepository;

    @Test
    void getList() {
        // given
        Notice notice = mock(Notice.class);
        given(notice.getType()).willReturn(NoticeType.NOTICE);
        given(notice.getTitle()).willReturn("title");
        given(notice.getContent()).willReturn("content");
        given(notice.getCreatedAt()).willReturn(LocalDateTime.of(2022, 12, 22, 2, 21));
        given(noticeRepository.findSliceByUseYnIsTrue(any(Pageable.class))).willReturn(new PageImpl<>(List.of(notice)));

        // when
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<NoticeDto> result = noticeService.getList(pageRequest);

        // then
        NoticeDto noticeDto = result.getContent().get(0);
        assertThat(noticeDto.getType()).isEqualTo(NoticeType.NOTICE);
        assertThat(noticeDto.getTitle()).isEqualTo("title");
        assertThat(noticeDto.getContent()).isEqualTo("content");
        assertThat(notice.getCreatedAt()).isEqualTo(LocalDateTime.of(2022, 12, 22, 2, 21));
    }

}
