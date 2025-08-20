package com.datn.school_service.Repository;

import com.datn.school_service.Models.Semester;
import com.datn.school_service.Models.Semester;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {


    Page<Semester> findAllByIsActiveTrue(Pageable pageable);

    Page<Semester> findAllByIsActiveFalse(Pageable pageable);

    boolean existsBySemesterIdAndIsActiveTrue(Long id);

    boolean existsBySemesterNameAndAndSchoolYear_SchoolYearId(String name,Long schoolYearId);

    List<Semester> findAllByIsActiveTrueAndSemesterNameContainingIgnoreCase(String keyword);

    List<Semester> findAllByIsActiveFalseAndSemesterNameContainingIgnoreCase(String keyword);

    List<Semester> findAllBySchoolYear_SchoolYearId(Long schoolYearId);


//    List<Semester>
}
