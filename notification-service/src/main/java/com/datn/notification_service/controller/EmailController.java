package com.datn.notification_service.controller;

import com.datn.notification_service.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public String sendHtmlEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String name,
            @RequestParam String message) {
        try {
            emailService.sendHtmlEmail(to, subject, name, message);
            return "HTML Email sent successfully!";
        } catch (Exception e) {
            return "Error sending HTML email: " + e.getMessage();
        }
    }
}
