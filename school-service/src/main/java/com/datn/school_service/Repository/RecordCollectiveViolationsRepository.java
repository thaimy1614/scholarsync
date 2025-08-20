package com.datn.school_service.Repository;

import com.datn.school_service.Models.RecordCollectiveViolations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecordCollectiveViolationsRepository extends JpaRepository<RecordCollectiveViolations, Long> {

    @Query("SELECT CASE WHEN FUNCTION('DATE', r.createdAt) < CURRENT_DATE THEN true ELSE false END FROM RecordCollectiveViolations r WHERE r.recordCollectiveViolationsId = :id")
    boolean isOverdue(@Param("id") Long id);

    List<RecordCollectiveViolations> findAllByClazz_ClassId(Long classId);

    Page<RecordCollectiveViolations> findAllByIsActiveTrue(Pageable pageable);

    Page<RecordCollectiveViolations> findAllByIsActiveFalse(Pageable pageable);

    @Query("""
                SELECT COUNT(r) > 0 
                FROM RecordCollectiveViolations r 
                WHERE r.clazz.classId = :classId 
                  AND FUNCTION('DATE', r.createdAt) = CURRENT_DATE
            """)
    boolean existsByClassIdAndCreatedToday(@Param("classId") Long classId);
//
    @Query("""
      SELECT COALESCE(SUM(v.violationPoint), 0)
      FROM RecordCollectiveViolations r
      JOIN r.violationTypes v
      WHERE r.recordCollectiveViolationsId = :recordId
        AND r.isActive = true
    """)
    int sumViolationPointsByRecordId(@Param("recordId") Long recordId);



    //

    @Query(value = """
    SELECT 
        EXTRACT(WEEK FROM r.created_at) AS week,
        SUM(v.violation_point) AS totalViolationPoint
    FROM record_collective_violations r
    JOIN record_collective_violation_type rvt ON r.record_collective_violations_id = rvt.record_collective_violations_id
    JOIN violation_type v ON rvt.violation_type_id = v.violation_type_id
    JOIN class c ON r.class_id = c.class_id
    JOIN school_year sy ON c.school_year_id = sy.school_year_id
    WHERE r.class_id = :classId
      AND r.created_at BETWEEN sy.start_date AND sy.end_date
      AND r.is_active = true
    GROUP BY EXTRACT(WEEK FROM r.created_at)
    ORDER BY week
""", nativeQuery = true)
    List<Object[]> getViolationPointsByWeekForClass(@Param("classId") Long classId);

//    @Query(value = """
//    SELECT
//        EXTRACT(WEEK FROM r.created_at) AS week,
//        c.class_name,
//        SUM(v.violation_point) AS totalViolationPoint
//    FROM record_collective_violations r
//    JOIN record_collective_violation_type rvt ON r.record_collective_violations_id = rvt.record_collective_violations_id
//    JOIN violation_type v ON rvt.violation_type_id = v.violation_type_id
//    JOIN class c ON r.class_id = c.class_id
//    JOIN school_year sy ON c.school_year_id = sy.school_year_id
//    WHERE sy.school_year_id = :schoolYearId
//      AND r.created_at BETWEEN sy.start_date AND sy.end_date
//      AND r.is_active = true
//    GROUP BY EXTRACT(WEEK FROM r.created_at), c.class_name
//    ORDER BY week, c.class_name
//""", nativeQuery = true)
//    List<Object[]> getAllClassWeeklyViolationsBySchoolYear(@Param("schoolYearId") Long schoolYearId);

    @Query(value = """
    SELECT 
        FLOOR(DATE_PART('day', r.created_at - sy.start_date) / 7) + 1 AS week,
        c.class_name,
        SUM(v.violation_point) AS totalViolationPoint
    FROM record_collective_violations r
    JOIN record_collective_violation_type rvt 
        ON r.record_collective_violations_id = rvt.record_collective_violations_id
    JOIN violation_type v 
        ON rvt.violation_type_id = v.violation_type_id
    JOIN class c 
        ON r.class_id = c.class_id
    JOIN school_year sy 
        ON c.school_year_id = sy.school_year_id
    WHERE sy.school_year_id = :schoolYearId
      AND r.created_at BETWEEN sy.start_date AND sy.end_date
      AND r.is_active = true
    GROUP BY week, c.class_name
    ORDER BY week, c.class_name
""", nativeQuery = true)
    List<Object[]> getAllClassWeeklyViolationsBySchoolYear(@Param("schoolYearId") Long schoolYearId);


    @Query(value = """
    SELECT 
        EXTRACT(WEEK FROM r.created_at) AS week,
        c.class_name,
        SUM(v.violation_point) AS totalViolationPoint
    FROM record_collective_violations r
    JOIN record_collective_violation_type rvt ON r.record_collective_violations_id = rvt.record_collective_violations_id
    JOIN violation_type v ON rvt.violation_type_id = v.violation_type_id
    JOIN class c ON r.class_id = c.class_id
    WHERE r.created_at BETWEEN :start AND :end
      AND r.is_active = true
    GROUP BY week, c.class_name
    ORDER BY week DESC
""", nativeQuery = true)
    List<Object[]> getWeeklyViolationPointsByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

}


