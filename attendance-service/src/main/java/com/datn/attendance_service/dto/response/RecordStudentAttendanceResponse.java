package com.datn.attendance_service.dto.response;

import com.datn.attendance_service.model.AttendanceRecord;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RecordStudentAttendanceResponse {
    private String studentId;

    private String fullName;

    private Long timetableId;

    private Long id;

    private Long classId;

    private String className;

    private Long subjectId;

    private String subjectName;

    private String roomName;

    private Long roomId;

    private String teacherId;

    private String teacherName;

    private int slot;

    private AttendanceRecord.Session session;

    private LocalDate attendanceDate;

    private AttendanceRecord.AttendanceStatus status;

    private Double attendanceScore;
}
