package com.datn.timetable_service.repository;

import com.datn.timetable_service.model.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    List<Timetable> findByClassIdAndSemesterIdAndWeekAndIsDeletedFalse(Long classId, Long semesterId, int week);

    List<Timetable> findByTeacherIdAndSemesterIdAndWeekAndIsDeletedFalse(String teacherId, Long semesterId, int week);

    List<Timetable> findBySemesterIdAndWeekAndIsDeletedFalse(Long semesterId, int week);

    List<Timetable> findBySemesterIdAndWeekAndDateBetweenAndIsDeletedFalse(
            Long semesterId, int week, LocalDate startDate, LocalDate endDate);

    @Query("SELECT t FROM Timetable t WHERE t.classId = :classId AND t.semesterId = :semesterId " +
            "AND t.week = :week AND t.dayOfWeek = :dayOfWeek AND t.slot = :slot AND t.isDeleted = false")
    List<Timetable> findByClassIdAndSemesterIdAndWeekAndDayOfWeekAndSlot(
            @Param("classId") Long classId, @Param("semesterId") Long semesterId,
            @Param("week") int week, @Param("dayOfWeek") int dayOfWeek, @Param("slot") int slot);

    @Query("SELECT t FROM Timetable t WHERE t.teacherId = :teacherId AND t.semesterId = :semesterId " +
            "AND t.week = :week AND t.dayOfWeek = :dayOfWeek AND t.slot = :slot AND t.isDeleted = false")
    List<Timetable> findByTeacherIdAndSemesterIdAndWeekAndDayOfWeekAndSlot(
            @Param("teacherId") String teacherId, @Param("semesterId") Long semesterId,
            @Param("week") int week, @Param("dayOfWeek") int dayOfWeek, @Param("slot") int slot);

    @Query("SELECT t FROM Timetable t WHERE t.roomId = :roomId AND t.semesterId = :semesterId " +
            "AND t.week = :week AND t.dayOfWeek = :dayOfWeek AND t.slot = :slot AND t.isDeleted = false")
    List<Timetable> findByRoomIdAndSemesterIdAndWeekAndDayOfWeekAndSlot(
            @Param("roomId") Long roomId, @Param("semesterId") Long semesterId,
            @Param("week") int week, @Param("dayOfWeek") int dayOfWeek, @Param("slot") int slot);

    @Modifying
    @Query("UPDATE Timetable t SET t.isDeleted = true WHERE t.id = :id")
    void softDeleteById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Timetable t SET t.isDeleted = false WHERE t.id = :id")
    void restoreById(@Param("id") Long id);

    List<Timetable> findByIsDeletedTrue();
}