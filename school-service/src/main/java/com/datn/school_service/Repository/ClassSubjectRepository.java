package com.datn.school_service.Repository;

import com.datn.school_service.Models.ClassSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassSubjectRepository extends JpaRepository<ClassSubject, Long> {

    boolean existsClassSubjectByClassIdAndSubjectId(Long classiId,Long subjectId);
}
