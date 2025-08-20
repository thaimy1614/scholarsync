package com.datn.school_service.Repository;

import com.datn.school_service.Models.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    Page<Answer> findAllByIsActiveTrue(Pageable pageable);

    Page<Answer> findAllByIsActiveFalse(Pageable pageable);

    Optional<Answer> findByAnswerIdAndIsActiveTrue(Long id);

    boolean existsByAnswer(String name);

    List<Answer> findAllByIsActiveTrueAndAnswerContainingIgnoreCase(String keyword);

    List<Answer> findAllByIsActiveFalseAndAnswerContainingIgnoreCase(String keyword);

    @Query("SELECT MAX(a.answerPoint) FROM Answer a JOIN a.questions q WHERE q.questionId = :questionId")
    Integer findMaxAnswerPointByQuestionId(@Param("questionId") Long questionId);

}
