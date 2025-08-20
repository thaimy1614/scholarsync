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
@Table(name = "attendance_summaries")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "class_id", nullable = false)
    private Long classId;

    @Column(name = "period_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    @Column(name = "period_start_date", nullable = false)
    private LocalDate periodStartDate;

    @Column(name = "period_end_date", nullable = false)
    private LocalDate periodEndDate;

    @Column(name = "total_slots", nullable = false)
    private Integer totalSlots;

    @Column(name = "present_slots", nullable = false)
    private Integer presentSlots;

    @Column(name = "absent_slots", nullable = false)
    private Integer absentSlots;

    @Column(name = "late_slots", nullable = false)
    private Integer lateSlots;

    @Column(name = "early_leave_slots", nullable = false)
    private Integer earlyLeaveSlots;

    @Column(name = "total_score", nullable = false)
    private Double totalScore;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PeriodType {
        DAY, WEEK, MONTH, SEMESTER
    }
}
