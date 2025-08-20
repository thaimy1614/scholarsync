package com.datn.timetable_service.dto.SchoolService;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class SemesterResponse {
    private Long semesterId;

    private String semesterName;

    private LocalDate startDate;

    private LocalDate endDate;

    private boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String description;
}