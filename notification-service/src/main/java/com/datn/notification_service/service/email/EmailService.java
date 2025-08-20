package com.datn.notification_service.service.email;

import com.datn.notification_service.dto.kafka.*;

public interface EmailService {
    void sendOtp(SendOTP sendOtp);

    void sendNewPassword(SendPassword sendPassword);

    void sendVerification(String name, String to, String url) throws Exception;

    void sendAccountInfo(AccountInfo accountInfo);

    void sendAttendanceReminderToParent(AttendanceReminderToParent attendanceReminderToParent);

    void sendWarningReminderToParent(WarningReminderToParent attendanceReminderToParent);

    void sendHtmlEmail(String to, String subject, String name, String message) throws Exception;
}

