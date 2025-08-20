package com.datn.school_service.Repository;

import com.datn.school_service.Models.RecordPersonalViolations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecordPersonalViolationsRepository extends JpaRepository<RecordPersonalViolations, Long> {

    @Query("""
      SELECT COALESCE(SUM(v.violationPoint), 0)
      FROM RecordPersonalViolations rp
      JOIN rp.violationTypes v
      WHERE rp.recordCollectiveViolations.recordCollectiveViolationsId = :collectiveId
        AND rp.isActive = true
    """)
    int sumPersonalPointsByCollectiveId(@Param("collectiveId") Long collectiveId);
}
