package com.datn.school_service.Repository;

import com.datn.school_service.Models.ViolationRecord;
import jakarta.persistence.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ViolationRecordRepository extends JpaRepository<ViolationRecord, Long> {

    @Query("SELECT v FROM ViolationRecord v LEFT JOIN v.clazz c WHERE v.isActive = :active")
    Page<ViolationRecord> findAllByIsActive(@Param("active") boolean active, Pageable pageable);
    List<ViolationRecord> findAllByClazz_ClassIdAndIsActiveTrue(Long classId);


}
