package com.datn.attendance_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;


@Entity
@Table(name = "attendance_records")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "timetable_id", nullable = false)
    private Long timetableId;

    @Column(name = "class_id", nullable = false)
    private Long classId;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "teacher_id", nullable = false)
    private String teacherId;

    @Column(name = "session", nullable = false)
    @Enumerated(EnumType.STRING)
    private Session session;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    @Column(name = "attendance_score", nullable = false)
    private Double attendanceScore = 10.0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Session {
        MORNING, AFTERNOON
    }

    public enum AttendanceStatus {
        PRESENT, ABSENT, LATE, EARLY_LEAVE
    }
}
