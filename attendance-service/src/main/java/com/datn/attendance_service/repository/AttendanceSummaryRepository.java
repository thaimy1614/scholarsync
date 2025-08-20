package com.datn.attendance_service.repository;

import com.datn.attendance_service.model.AttendanceSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceSummaryRepository extends JpaRepository<AttendanceSummary, Long> {
    List<AttendanceSummary> findByStudentIdAndPeriodTypeAndPeriodStartDateBetween(
            String studentId, AttendanceSummary.PeriodType periodType, LocalDate startDate, LocalDate endDate);
}