package com.datn.timetable_service.dto.SchoolService;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class SchoolYearResponse {
    private Long schoolYearId;

    private String schoolYear;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

    private boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}