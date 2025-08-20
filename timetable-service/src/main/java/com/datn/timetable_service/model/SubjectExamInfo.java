package com.datn.timetable_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subject_exam_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectExamInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long subjectId;

    @Column(nullable = false)
    private Integer duration; // in minutes

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamType type;

    public enum ExamType {
        THEORY, PRACTICAL
    }
}