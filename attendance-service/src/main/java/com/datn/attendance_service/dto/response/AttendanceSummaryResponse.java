package com.datn.attendance_service.dto.response;

import com.datn.attendance_service.model.AttendanceSummary;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AttendanceSummaryResponse {
    private Long id;

    private String studentId;

    private String fullName;

    private Long classId;

    private String className;

    private AttendanceSummary.PeriodType periodType;

    private LocalDate periodStartDate;

    private LocalDate periodEndDate;

    private Integer totalSlots;

    private Integer presentSlots;

    private Integer absentSlots;

    private Integer lateSlots;

    private Integer earlyLeaveSlots;

    private Double totalScore;
}
