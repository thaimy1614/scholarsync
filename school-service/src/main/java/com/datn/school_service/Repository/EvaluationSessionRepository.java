package com.datn.school_service.Repository;

import com.datn.school_service.Models.EvaluationSession;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationSessionRepository extends CrudRepository<EvaluationSession, Long> {

    Optional<EvaluationSession> findByStudentIdAndTeacherIdAndSemester_SemesterIdAndClazz_ClassId(
            String studentId,
            String teacherId,
            Long semesterId,
            Long classId
    );
    Optional<EvaluationSession> findByStudentIdAndTeacherIdAndSemester_SemesterIdAndClazz_ClassIdAndSubjectId(
            String studentId,
            String teacherId,
            Long semesterId,
            Long classId,
            Long subjectId
    );

    @Query("SELECT e FROM EvaluationSession e " +
            "WHERE e.studentId = :studentId " +
            "AND e.teacherId = :teacherId " +
            "AND e.semester.semesterId = :semesterId")
    Optional<EvaluationSession> findSessionIgnoreClass(
            @Param("studentId") String studentId,
            @Param("teacherId") String teacherId,
            @Param("semesterId") Long semesterId);

    boolean existsByEvaluationSessionIdAndIsActiveTrue(Long evaluationSessionId);
    @Query("SELECT e.studentId FROM EvaluationSession e " +
            "WHERE e.clazz.classId = :classId " +
            "AND e.semester.semesterId = :semesterId " +
            "AND e.teacherId = :teacherId")
    List<String> findStudentIdsByClassSemesterTeacher(
            @Param("classId") Long classId,
            @Param("semesterId") Long semesterId,
            @Param("teacherId") String teacherId
    );

    @Query("SELECT AVG(e.averageReportPoint) " +
            "FROM EvaluationSession e " +
            "WHERE e.teacherId = :teacherId " +
            "AND e.subjectId = :subjectId " +
            "AND e.semester.semesterId = :semesterId " +
            "AND (:classId IS NULL OR e.clazz.classId = :classId)")
    Double calculateAverageReportPoint(
            @Param("teacherId") String teacherId,
            @Param("subjectId") Long subjectId,
            @Param("semesterId") Long semesterId,
            @Param("classId") Long classId
    );

    List<EvaluationSession> findAllByClazz_ClassIdAndTeacherIdAndSemester_SemesterId(Long classId, String teacherId, Long semesterId);

    List<EvaluationSession> findAllByTeacherIdAndSemester_SemesterId(String teacherId, Long semesterId);

    List<EvaluationSession> findAllBySemester_SemesterId(Long semesterId);
}
