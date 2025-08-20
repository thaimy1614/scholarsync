package com.datn.timetable_service.repository;

import com.datn.timetable_service.model.ExamSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {
    List<ExamSession> findBySemesterId(Long semesterId);
    List<ExamSession> findBySchoolYearId(Long schoolYearId);
    List<ExamSession> findBySemesterIdAndSchoolYearId(Long semesterId, Long schoolYearId);
}