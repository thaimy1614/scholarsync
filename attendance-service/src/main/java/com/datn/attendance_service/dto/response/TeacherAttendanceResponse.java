package com.datn.attendance_service.dto.response;

import com.datn.attendance_service.model.TeacherAttendance;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TeacherAttendanceResponse {
    private Long id;

    private String teacherId;

    private String teacherName;

    private String image;

    private LocalDate attendanceDate;

    private TeacherAttendance.AttendanceStatus status;

    private String recordedById;

    private String recordedByName;
}
