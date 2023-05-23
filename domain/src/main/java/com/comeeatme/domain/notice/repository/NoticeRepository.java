package com.comeeatme.domain.notice.repository;

import com.comeeatme.domain.notice.Notice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Slice<Notice> findSliceByUseYnIsTrue(Pageable pageable);

}
