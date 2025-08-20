package com.datn.school_service.Repository;

import com.datn.school_service.Models.TeacherClassification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeacherClassificationRepository extends JpaRepository<TeacherClassification, Long> {
    Page<TeacherClassification> findAllByIsActiveTrue(Pageable pageable);

    Page<TeacherClassification> findAllByIsActiveFalse(Pageable pageable);

    Optional<TeacherClassification> findByTeacherIdAndSubjectIdAndSemester_SemesterIdAndClazz_ClassId(
            String teacherId,
            Long subjectId,
            Long semesterId,
            Long classId
    );

    List<TeacherClassification> findAllByTeacherClassificationName(String teacherclassificationName);

    @Query("""
        SELECT COALESCE(AVG(t.teacherClassificationPoint), 0)
        FROM TeacherClassification t
        WHERE t.teacherId = :teacherId
        AND t.semester.semesterId = :semesterId
    """)
    double getEvargaByTeacherAndSemester(
            @Param("teacherId") String teacherId,
            @Param("semesterId") Long semesterId
    );

    boolean existsByTeacherIdAndSemester_SemesterId(String teacherId,Long semesterId);
}
