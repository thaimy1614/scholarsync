package com.datn.notification_service.service.message;

import com.datn.notification_service.dto.kafka.*;
import org.springframework.kafka.annotation.KafkaListener;

public interface MessageService {
    @KafkaListener(id = "sendOtpGroup", topics = "sendOTP")
    void listenSendOtp(SendOTP sendOtp);

    @KafkaListener(id = "sendPasswordGroup", topics = "sendNewPassword")
    void listenSendPassword(SendPassword sendPassword);

    @KafkaListener(id = "verificationGroup", topics = "verification")
    void listenVerification(VerifyAccount verifyAccount) throws Exception;

    @KafkaListener(id = "sendAccountInfoGroup", topics = "sendAccountInfo")
    void listenSendAccountInfo(AccountInfo accountInfo);

    @KafkaListener(id = "sendAttendanceToParentGroup", topics = "sendAttendanceReminderToParent")
    void listenSendAttendanceReminderToParent(AttendanceReminderToParent attendanceReminderToParent);

    @KafkaListener(id = "warningReminderToParentGroup", topics = "sendWarningReminderToParent")
    void listenSendWarningReminderToParent(WarningReminderToParent warningReminderToParent);

}
