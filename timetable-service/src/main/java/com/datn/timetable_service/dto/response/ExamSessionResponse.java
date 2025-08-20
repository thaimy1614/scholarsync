package com.datn.timetable_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamSessionResponse {
    private Long examSessionId;
    private Long semesterId;
    private String semesterName;
    private Long schoolYearId;
    private String schoolYear;
    private LocalDate startDate;
    private LocalDate endDate;
}
