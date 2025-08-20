package com.datn.timetable_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "exam_schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long classId;

    @Column(nullable = false)
    private Long subjectId;

    @Column(nullable = false)
    private LocalDate examDate;

    @Column(nullable = false)
    private LocalTime examTime;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private String teacherId;

    @Column(nullable = false)
    private Long semesterId;

    @Column(nullable = false)
    private Long schoolYearId;

    public ExamSchedule(ExamSchedule examSchedule) {
        this.id = examSchedule.id;
        this.classId = examSchedule.classId;
        this.subjectId = examSchedule.subjectId;
        this.examDate = examSchedule.examDate;
        this.examTime = examSchedule.examTime;
        this.roomId = examSchedule.roomId;
        this.teacherId = examSchedule.teacherId;
        this.semesterId = examSchedule.semesterId;
        this.schoolYearId = examSchedule.schoolYearId;
    }
}
