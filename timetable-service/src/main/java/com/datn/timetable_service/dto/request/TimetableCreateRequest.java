package com.datn.timetable_service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimetableCreateRequest {
    @NotNull(message = "Class ID is required")
    private Long classId;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotBlank(message = "Teacher ID is required")
    private String teacherId;

    @Min(value = 1, message = "Day of week must be between 1 and 6")
    @Max(value = 6, message = "Day of week must be between 1 and 6")
    private int dayOfWeek;

    @Min(value = 1, message = "Slot must be between 1 and 10")
    @Max(value = 10, message = "Slot must be between 1 and 10")
    private int slot;

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @Min(value = 1, message = "Week number must be positive")
    private int week;

    @NotNull(message = "Semester ID is required")
    private Long semesterId;

    @NotNull(message = "School Year ID is required")
    private Long schoolYearId;

    @NotNull(message = "Date is required")
    private LocalDate date;
}
