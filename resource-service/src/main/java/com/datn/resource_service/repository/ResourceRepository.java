package com.datn.resource_service.repository;

import com.datn.resource_service.model.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    Page<Resource> findAllByResourceNameContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Resource> findAllBySubjectIdAndGradeId(Long subjectId, Long gradeId, Pageable pageable);
}
