package com.comeeatme.domain.report.repository;

import com.comeeatme.domain.report.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
