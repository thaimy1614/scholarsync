package com.datn.timetable_service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimetableUpdateRequest {
    @Min(value = 1, message = "Day of week must be between 1 and 6")
    @Max(value = 6, message = "Day of week must be between 1 and 6")
    private Integer dayOfWeek;

    @Min(value = 1, message = "Slot must be between 1 and 10")
    @Max(value = 10, message = "Slot must be between 1 and 10")
    private Integer slot;

    private String teacherId;
    private Long roomId;
    private LocalDate date;
}