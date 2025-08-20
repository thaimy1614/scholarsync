package com.datn.notification_service.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceReminderToParent {
    private String studentName;
    private String status;
    private String parentEmail;
    private String session;
    private int slotNumber;
    private LocalDate attendanceDate;
}
