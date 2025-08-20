package com.datn.school_service.Services.TeacherClassification;

import com.datn.school_service.Dto.Request.Teacherlassification.AddTeacherlassificationRequest;
import com.datn.school_service.Dto.Request.Teacherlassification.DescriptionRequest;
import com.datn.school_service.Dto.Request.Teacherlassification.SearchTeacherlassification;
import com.datn.school_service.Dto.Request.Teacherlassification.TeacherClassificationInSemesterRequest;
import com.datn.school_service.Dto.Respone.Teacherlassification.TeacherClassificationInSemesterResponse;
import com.datn.school_service.Dto.Respone.Teacherlassification.TeacherlassificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TeacherClassificationInterface
{
   TeacherlassificationResponse updateTeacherlassification(Long id,AddTeacherlassificationRequest addTeacherlassificationRequest);
    boolean deleteTeacherlassification(Long id);
    TeacherlassificationResponse getTeacherlassificationById(Long id);
    List<TeacherlassificationResponse> searchTeacherlassification(SearchTeacherlassification searchTeacherlassification);
    Page<TeacherlassificationResponse> getAll(Pageable pageable, boolean active);
    String checkteacherClassificationName(double point);

    void promoteTeacherClassification(Long teacherClassificationId, DescriptionRequest descriptionRequest);
    void demoteTeacherClassification(Long teacherClassificationId, DescriptionRequest descriptionRequest);

  TeacherClassificationInSemesterResponse getTeacherClassificationInSemester(TeacherClassificationInSemesterRequest teacherClassificationInSemesterRequest);
//
// List<TeacherClassificationInSemesterResponse> getAllTeacherClassificationInSemester(TeacherClassificationInSemesterRequest teacherClassificationInSemesterRequest);

}
