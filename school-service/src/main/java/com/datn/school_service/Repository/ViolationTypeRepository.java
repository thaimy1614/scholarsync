package com.datn.school_service.Repository;

import com.datn.school_service.Models.ViolationType;
import com.datn.school_service.Models.ViolationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ViolationTypeRepository extends JpaRepository<ViolationType, Long> {

    Page<ViolationType> findAllByIsActiveTrue(Pageable pageable);

    Page<ViolationType> findAllByIsActiveFalse(Pageable pageable);

    Optional<ViolationType> findByViolationTypeIdAndIsActiveTrue(Long id);

    boolean existsByViolationTypeName(String name);

    List<ViolationType> findAllByIsActiveTrueAndViolationTypeNameContainingIgnoreCase(String keyword);

    List<ViolationType> findAllByIsActiveFalseAndViolationTypeNameContainingIgnoreCase(String keyword);

}
