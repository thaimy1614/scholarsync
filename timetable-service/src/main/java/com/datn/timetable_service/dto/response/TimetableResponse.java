package com.datn.timetable_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimetableResponse {
    private Long id;
    private Long classId;
    private String className;
    private Long subjectId;
    private String subjectName;
    private String teacherId;
    private String teacherName;
    private int dayOfWeek;
    private int slot;
    private Long roomId;
    private String roomName;
    private int week;
    private Long semesterId;
    private Long schoolYearId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isDeleted;
}
