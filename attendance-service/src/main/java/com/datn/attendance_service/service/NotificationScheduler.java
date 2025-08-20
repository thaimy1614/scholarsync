package com.datn.attendance_service.service;

import com.datn.attendance_service.client.TimetableClient;
import com.datn.attendance_service.model.Notification;
import com.datn.attendance_service.repository.AttendanceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class NotificationScheduler {
    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private TimetableClient timetableClient;
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Scheduled(cron = "0 0 19 * * ?") // 7 PM hàng ngày
    public void checkAndSendNotifications() {
        attendanceService.checkAbsencesAndSendWarnings(LocalDate.now());
    }
}