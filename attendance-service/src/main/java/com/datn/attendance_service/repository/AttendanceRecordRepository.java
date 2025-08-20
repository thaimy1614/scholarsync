package com.datn.attendance_service.repository;

import com.datn.attendance_service.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByStudentIdAndAttendanceDateBetween(String studentId, LocalDate startDate, LocalDate endDate);
    List<AttendanceRecord> findByStudentIdAndSubjectIdAndAttendanceDateBetween(String studentId, Long subjectId, LocalDate startDate, LocalDate endDate);
    List<AttendanceRecord> findByClassIdAndAttendanceDate(Long classId, LocalDate date);
    List<AttendanceRecord> findByTeacherIdAndAttendanceDateBetween(String teacherId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT a FROM AttendanceRecord a WHERE a.studentId = :studentId AND a.status = 'ABSENT' AND a.attendanceDate BETWEEN :startDate AND :endDate")
    List<AttendanceRecord> findAbsentRecordsByStudentAndDateRange(String studentId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT a FROM AttendanceRecord a WHERE a.status = 'ABSENT' AND a.attendanceDate BETWEEN :startDate AND :endDate")
    List<AttendanceRecord> findAbsentRecordsByDateRange(LocalDate startDate, LocalDate endDate);

    boolean existsByStudentIdAndTimetableId(String studentId, Long timetableId);

    Optional<AttendanceRecord> findByStudentIdAndTimetableId(String studentId, Long timetableId);

    List<AttendanceRecord> findAllByTimetableId(Long timetableId);

    boolean existsByTimetableId(Long id);

    List<AttendanceRecord> findByClassIdAndSubjectIdAndAttendanceDate(Long classId, Long subjectId, LocalDate date);
}