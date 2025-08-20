package com.datn.school_service.Repository;

import com.datn.school_service.Models.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question,Long> {
    Page<Question> findAllByIsActiveTrue(Pageable pageable);

    Page<Question> findAllByIsActiveFalse(Pageable pageable);

    boolean existsByQuestionIdAndIsActiveTrue(Long id);

    boolean existsByQuestionId(Long id);

    boolean existsByQuestion(String name);

    List<Question> findAllByIsActiveTrueAndQuestionContainingIgnoreCase(String keyword);

    List<Question> findAllByIsActiveFalseAndQuestionContainingIgnoreCase(String keyword);
}
