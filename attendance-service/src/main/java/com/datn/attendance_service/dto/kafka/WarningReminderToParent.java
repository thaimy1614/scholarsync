package com.datn.attendance_service.dto.kafka;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WarningReminderToParent {
    private String studentName;
    private String parentEmail;
    private String totalAbsences;
}
