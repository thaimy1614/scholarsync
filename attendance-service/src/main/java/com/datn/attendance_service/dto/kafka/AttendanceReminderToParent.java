package com.datn.attendance_service.dto.kafka;

import com.datn.attendance_service.model.AttendanceRecord;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
public class AttendanceReminderToParent {
    private String studentName;
    private String status;
    private String parentEmail;
    private String session;
    private int slotNumber;
    private LocalDate attendanceDate;
}
