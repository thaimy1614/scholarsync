package com.datn.attendance_service.dto.request;

import com.datn.attendance_service.model.AttendanceRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RecordStudentAttendanceRequest {
    private Long timetableId;
    @Schema(description = "Attendance status", example = "PRESENT/ABSENT/LATE/EARLY_LEAVE")
    private AttendanceRecord.AttendanceStatus status;
    private LocalDate attendanceDate;
    private String studentId;
}
