package com.datn.notification_service.service.email;

import com.datn.notification_service.dto.kafka.*;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
class EmailServiceImpl implements EmailService {
    private final TemplateEngine templateEngine;
    @Lazy
    private final Gmail gmailService;

    public void sendHtmlEmail(String to, String subject, String name, String message) throws Exception {
        Context context = new Context();
        context.setVariable("subject", subject);
        context.setVariable("name", name);
        context.setVariable("message", message);
        String htmlContent = templateEngine.process("email-template", context);
        String emailContent = createEmailContent(to, "scholarsync.nohope@gmail.com", subject, htmlContent);
        sendMessage(gmailService, "me", emailContent);
    }

    private String createEmailContent(String to, String from, String subject, String htmlContent) {
        StringBuilder email = new StringBuilder();
        email.append("To: ").append(to).append("\r\n");
        email.append("From: ").append(from).append("\r\n");
        email.append("Subject: ").append(subject).append("\r\n");
        email.append("MIME-Version: 1.0\r\n");
        email.append("Content-Type: text/html; charset=UTF-8\r\n");
        email.append("\r\n"); // Dòng trống ngăn cách header và body
        email.append(htmlContent);
        return email.toString();
    }

    private void sendMessage(Gmail service, String userId, String emailContent) throws Exception {
        String encodedEmail = Base64.getUrlEncoder()
                .encodeToString(emailContent.getBytes(StandardCharsets.UTF_8));
        Message message = new Message();
        message.setRaw(encodedEmail);
        service.users().messages().send(userId, message).execute();
    }

    @Async
    public void sendVerification(String name, String to, String url) throws Exception {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("url", url);
        String htmlContent = templateEngine.process("verify-account", context);
        String emailContent = createEmailContent(to, "scholarsync.nohope@gmail.com", "ScholarSync - Verify Your Account", htmlContent);
        sendMessage(gmailService, "me", emailContent);
    }

    @Async
    public void sendAccountInfo(AccountInfo accountInfo) {
        try {
            Context context = new Context();
            context.setVariable("name", accountInfo.getFullName());
            context.setVariable("username", accountInfo.getUsername());
            context.setVariable("password", accountInfo.getPassword());
            String htmlContent = templateEngine.process("send-account", context);
            String emailContent = createEmailContent(accountInfo.getEmail(), "scholarsync.nohope@gmail.com", "ScholarSync - Your Account Is Ready!", htmlContent);
            sendMessage(gmailService, "me", emailContent);
        } catch (Exception e) {
            throw new RuntimeException("Error sending account info email via Brevo", e);
        }
    }

    @Override
    @Async
    public void sendOtp(SendOTP sendOtp) {
        try {
            Context context = new Context();
            context.setVariable("name", sendOtp.getName());
            context.setVariable("otp", sendOtp.getOtp());
            String htmlContent = templateEngine.process("send-otp", context);
            String emailContent = createEmailContent(sendOtp.getEmail(), "scholarsync.nohope@gmail.com", "ScholarSync - Your OTP FOR Is Ready!", htmlContent);
            sendMessage(gmailService, "me", emailContent);
        } catch (Exception e) {
            throw new RuntimeException("Error sending OTP email via Brevo", e);
        }
    }

    @Async
    public void sendNewPassword(SendPassword sendPassword) {
        try {
            Context context = new Context();
            context.setVariable("name", sendPassword.getName());
            context.setVariable("username", sendPassword.getUsername());
            context.setVariable("password", sendPassword.getPassword());
            String htmlContent = templateEngine.process("send-new-password", context);
            String emailContent = createEmailContent(sendPassword.getEmail(), "scholarsync.nohope@gmail.com", "ScholarSync - Your New Password Is Ready", htmlContent);
            sendMessage(gmailService, "me", emailContent);
        } catch (Exception e) {
            throw new RuntimeException("Error sending new password email via Brevo", e);
        }
    }

    @Async
    public void sendAttendanceReminderToParent(AttendanceReminderToParent attendanceReminderToParent) {
        try {
            Context context = new Context();
            context.setVariable("studentName", attendanceReminderToParent.getStudentName());
            context.setVariable("slotNumber", attendanceReminderToParent.getSlotNumber());
            context.setVariable("session", attendanceReminderToParent.getSession());
            context.setVariable("status", attendanceReminderToParent.getStatus());
            context.setVariable("attendanceDate", attendanceReminderToParent.getAttendanceDate());
            String htmlContent = templateEngine.process("attendance-reminder-to-parent", context);
            String emailContent = createEmailContent(attendanceReminderToParent.getParentEmail(), "scholarsync.nohope@gmail.com", "ScholarSync - Student Attendance REMINDER!", htmlContent);
            sendMessage(gmailService, "me", emailContent);
        } catch (Exception e) {
            throw new RuntimeException("Error sending account info email via Brevo", e);
        }
    }

    @Async
    public void sendWarningReminderToParent(WarningReminderToParent warningReminderToParent) {
        try {
            Context context = new Context();
            context.setVariable("studentName", warningReminderToParent.getStudentName());
            context.setVariable("totalAbsences", warningReminderToParent.getTotalAbsences());
            String htmlContent = templateEngine.process("warning-reminder-to-parent", context);
            String emailContent = createEmailContent(warningReminderToParent.getParentEmail(), "scholarsync.nohope@gmail.com", "ScholarSync - Warning Attendance of Student!", htmlContent);
            sendMessage(gmailService, "me", emailContent);
        } catch (Exception e) {
            throw new RuntimeException("Error sending account info email via Brevo", e);
        }
    }
}

