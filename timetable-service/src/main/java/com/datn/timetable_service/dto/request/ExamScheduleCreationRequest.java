package com.datn.timetable_service.dto.request;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class ExamScheduleCreationRequest {
    private Long classId;

    private Long subjectId;

    private LocalDate examDate;

    private String examTime;

    private Long roomId;

    private String teacherId;

    private Long semesterId;

    private Long schoolYearId;
}
