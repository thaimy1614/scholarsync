package com.datn.attendance_service.repository;

import com.datn.attendance_service.model.TeacherAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TeacherAttendanceRepository extends JpaRepository<TeacherAttendance, Long> {
    List<TeacherAttendance> findByTeacherIdAndAttendanceDateBetween(String teacherId, LocalDate startDate, LocalDate endDate);
}
