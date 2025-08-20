package com.datn.attendance_service.dto.timetable_service;

import com.datn.attendance_service.model.AttendanceRecord;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SlotDetailResponse {
    private Long id;
    private Long classId;
    private Long subjectId;
    private String teacherId;
    private int dayOfWeek;
    private int slot;
    private Long roomId;
    private AttendanceRecord.Session mainSession; // MORNING or AFTERNOON
    private int week;
    private Long semesterId;
    private Long schoolYearId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
}