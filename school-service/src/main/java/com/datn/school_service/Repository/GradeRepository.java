package com.datn.school_service.Repository;

import com.datn.school_service.Models.Grade;
import com.datn.school_service.Models.News;
import com.datn.school_service.Models.Grade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    Page<Grade> findAllByIsActiveTrue(Pageable pageable);

    Page<Grade> findAllByIsActiveFalse(Pageable pageable);

    boolean existsByGradeIdAndIsActiveTrue(Long id);

    boolean existsByGradeName(String name);

    List<Grade> findAllByIsActiveTrueAndGradeNameContainingIgnoreCase(String keyword);

    List<Grade> findAllByIsActiveFalseAndGradeNameContainingIgnoreCase(String keyword);

    List<Grade> findAllByGradeIdInAndIsActiveTrue(List<Long> gradeIds);
}
