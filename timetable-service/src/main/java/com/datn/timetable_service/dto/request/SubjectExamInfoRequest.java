package com.datn.timetable_service.dto.request;

import com.datn.timetable_service.model.SubjectExamInfo.ExamType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectExamInfoRequest {
    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotNull(message = "Duration is required")
    @Min(value = 30, message = "Duration must be at least 30 minutes")
    private Integer duration;

    @NotNull(message = "Exam type is required")
    private ExamType type;
}