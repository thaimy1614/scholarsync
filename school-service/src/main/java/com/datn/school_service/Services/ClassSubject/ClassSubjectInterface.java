package com.datn.school_service.Services.ClassSubject;
import com.datn.school_service.Dto.Request.ClassSubject.AddClassSubjectRequest;
import com.datn.school_service.Dto.Request.ClassSubject.SearchClassSubjectRequest;
import com.datn.school_service.Dto.Respone.ClassSubjectResponse.ClassSubjectResponse;
import com.datn.school_service.Models.ClassSubject;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.List;

public interface ClassSubjectInterface {
    ClassSubjectResponse addClassSubject(AddClassSubjectRequest addClassSubjectRequest);
    void deleteClassSubject(Long id);
    void restoreClassSubject(Long id);
    ClassSubjectResponse updateClassSubject(Long id,AddClassSubjectRequest addClassSubjectRequest);
    ClassSubjectResponse getClassSubjectById(Long id);
    Page<ClassSubjectResponse> getAllClassSubjects(Pageable pageable,boolean active);
    List<ClassSubjectResponse> searchClassSubjectsByClassName(SearchClassSubjectRequest searchClassSubjectRequest);
}
