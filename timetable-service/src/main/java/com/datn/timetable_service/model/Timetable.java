package com.datn.timetable_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "timetable")
public class Timetable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private transient String tempId;
    private Long classId;
    private Long subjectId;
    private String teacherId;
    private int dayOfWeek; // 1 (Monday) to 6 (Saturday)
    private int slot; // 1-10
    private Long roomId;
    private int week; // Week number within the semester
    private Long semesterId;
    private Long schoolYearId;
    private LocalDate date; // Exact date for the slot
    private boolean isDeleted = false;
    private boolean isFixed;

    public Timetable(Timetable timetable) {
        this.id = timetable.id;
        this.tempId = timetable.tempId;
        this.classId = timetable.classId;
        this.subjectId = timetable.subjectId;
        this.teacherId = timetable.teacherId;
        this.dayOfWeek = timetable.dayOfWeek;
        this.slot = timetable.slot;
        this.roomId = timetable.roomId;
        this.week = timetable.week;
        this.semesterId = timetable.semesterId;
        this.schoolYearId = timetable.schoolYearId;
        this.date = timetable.date;
        this.isDeleted = timetable.isDeleted;
        this.isFixed = timetable.isFixed;

    }
}