package com.datn.school_service.Repository;

import com.datn.school_service.Models.StudentReportDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentReportDetailRepository extends JpaRepository<StudentReportDetail, Long> {
    boolean existsByEvaluationSession_EvaluationSessionIdAndQuestion_QuestionId(
            Long evaluationSessionId,
            Long questionId
    );
    @Query("SELECT SUM(d.answer.answerPoint) FROM StudentReportDetail d WHERE d.evaluationSession.evaluationSessionId = :sessionId AND d.isActive = true")
    Integer sumAnswerPointsByEvaluationSession(Long sessionId);
    boolean existsByStudentReportDetailIdAndIsActiveTrue(Long studentReportDetailId);

    @Query("SELECT COUNT(d) FROM StudentReportDetail d WHERE d.evaluationSession.evaluationSessionId = :sessionId AND d.isActive = true")
    Long countByEvaluationSessionId(Long sessionId);

    @Query("SELECT AVG(d.averagePoint) FROM StudentReportDetail d WHERE d.evaluationSession.evaluationSessionId = :sessionId AND d.isActive = true")
    Double averageAnswerPointByEvaluationSession(Long sessionId);

    List<StudentReportDetail> findByEvaluationSession_EvaluationSessionId(Long evaluationSessionId);

    }
