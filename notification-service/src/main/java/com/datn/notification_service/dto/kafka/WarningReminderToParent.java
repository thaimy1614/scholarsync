package com.datn.notification_service.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarningReminderToParent {
    private String studentName;
    private String parentEmail;
    private String totalAbsences;
}
