package com.datn.school_service.Repository;

import com.datn.school_service.Models.SchoolYear;
import com.datn.school_service.Models.SchoolYear;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface SchoolYearRepository extends JpaRepository<SchoolYear, Long> {
    SchoolYear findSchoolYearBySchoolYear(String schoolYear);

    Page<SchoolYear> findAllByIsActiveTrue(Pageable pageable);

    Page<SchoolYear> findAllByIsActiveFalse(Pageable pageable);

    Optional<SchoolYear> findBySchoolYearIdAndIsActiveTrue(Long id);

    boolean existsBySchoolYear(String name);

    List<SchoolYear> findAllByIsActiveTrueAndSchoolYearContainingIgnoreCase(String keyword);

    List<SchoolYear> findAllByIsActiveFalseAndSchoolYearContainingIgnoreCase(String keyword);


    @Query("SELECT s.schoolYearId FROM SchoolYear s WHERE s.schoolYear = :schoolYear")
    Long findSchoolYearIdBySchoolYear(@Param("schoolYear") String schoolYear);

    @Query("SELECT s FROM SchoolYear s WHERE s.startDate <= :date AND s.endDate >= :date")
    Optional<SchoolYear> findByDateBetween(@Param("date") LocalDate date);

    boolean existsByStartDateAndEndDate(LocalDate startDate, LocalDate endDate);

    boolean existsByStartDate(LocalDate startDate);

    boolean existsByEndDate(LocalDate endDate);

}
