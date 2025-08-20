package com.datn.timetable_service.repository;

import com.datn.timetable_service.model.SubjectExamInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface SubjectExamInfoRepository extends JpaRepository<SubjectExamInfo, Long> {
    Optional<SubjectExamInfo> findBySubjectId(Long subjectId);
    boolean existsBySubjectId(Long subjectId);

    List<SubjectExamInfo> findBySubjectIdIn(List<Long> subjectIds);
}