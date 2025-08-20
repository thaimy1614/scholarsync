package com.datn.attendance_service.dto.response;

import com.datn.attendance_service.model.TeacherAttendance;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TeacherAttendanceHistoryResponse {
    private Long id;

    private LocalDate attendanceDate;

    private TeacherAttendance.AttendanceStatus status;

    private String recordedById;

    private String recordedByName;
}
