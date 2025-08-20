package com.datn.timetable_service.dto.request;

import com.datn.timetable_service.model.SubjectExamInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetExamScheduleResponse {
    private Long id;

    private Long classId;

    private String className;

    private Long subjectId;

    private String subjectName;

    private LocalDate examDate;

    private LocalTime examTime;

    private Long roomId;

    private String roomName;

    private String teacherId;

    private String teacherName;

    private Long semesterId;

    private Long schoolYearId;

    private Integer duration;

    private SubjectExamInfo.ExamType type;
}
