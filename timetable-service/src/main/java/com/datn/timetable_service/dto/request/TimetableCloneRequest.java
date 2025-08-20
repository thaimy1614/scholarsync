package com.datn.timetable_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimetableCloneRequest {
    private Long sourceSemesterId;
    private int sourceWeek;
    private Long targetSemesterId;
    private int targetWeek;
    private LocalDate targetWeekStartDate;

}
