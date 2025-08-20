package com.datn.timetable_service.repository;

import com.datn.timetable_service.model.ExamSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long> {
    List<ExamSchedule> findByClassIdAndExamDateBetween(Long classId, LocalDate startDate, LocalDate endDate);

    List<ExamSchedule> findByTeacherIdAndExamDateBetween(String teacherId, LocalDate localDate, LocalDate localDate1);

    List<ExamSchedule> findByClassIdAndSemesterId(Long classId, Long semesterId);

    boolean existsByTeacherIdAndExamDateAndExamTimeAndClassId(String teacherId, LocalDate examDate, LocalTime examTime, Long classId);

    List<ExamSchedule> findByTeacherIdAndExamDate(String teacherId, LocalDate examDate);

    List<ExamSchedule> findByClassIdAndExamDate(Long classId, LocalDate examDate);
}
