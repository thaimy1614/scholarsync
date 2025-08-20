package com.datn.notification_service.service.message;

import com.datn.notification_service.dto.kafka.*;
import com.datn.notification_service.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {
    private final EmailService emailService;

    public void listenSendOtp(SendOTP sendOtp) {
        log.info("Received SendOTP message: {}", sendOtp);
        emailService.sendOtp(sendOtp);
    }

    public void listenSendPassword(SendPassword sendPassword) {
        log.info("Received SendPassword message: {}", sendPassword);
        emailService.sendNewPassword(sendPassword);
    }

    public void listenVerification(VerifyAccount verifyAccount) throws Exception {
        log.info("Received VerifyAccount message: {}", verifyAccount);
        emailService.sendVerification(verifyAccount.getFullName(), verifyAccount.getEmail(), verifyAccount.getUrl());
    }

    public void listenSendAccountInfo(AccountInfo accountInfo) {
        log.info("Received AccountInfo message: {}", accountInfo);
        emailService.sendAccountInfo(accountInfo);
    }

    public void listenSendAttendanceReminderToParent(AttendanceReminderToParent attendanceReminderToParent) {
        log.info("Received AttendanceReminderToParent message: {}", attendanceReminderToParent);
        emailService.sendAttendanceReminderToParent(attendanceReminderToParent);
    }

    public void listenSendWarningReminderToParent(WarningReminderToParent warningReminderToParent) {
        log.info("Received WarningReminderToParent message: {}", warningReminderToParent);
        emailService.sendWarningReminderToParent(warningReminderToParent);
    }
}
