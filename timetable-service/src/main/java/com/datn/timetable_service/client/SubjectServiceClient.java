package com.datn.timetable_service.client;

import com.datn.timetable_service.dto.ApiResponse;
import com.datn.timetable_service.dto.subject_service.ItemResponse;
import com.datn.timetable_service.dto.subject_service.RawTeacherSubjectClassResponse;
import com.datn.timetable_service.dto.subject_service.SubjectResponse;
import com.datn.timetable_service.dto.subject_service.TeacherSubjectClassResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "subject-service", url = "${application.clients.subject-service}/subject")
public interface SubjectServiceClient {
    @GetMapping("/teacher-subject-class/get-all-teacher-subject-classes-detail-by/classId/{classId}")
    ApiResponse<List<RawTeacherSubjectClassResponse>> getTeacherSubjectByClass(
            @PathVariable("classId") Long classId
    );

    @GetMapping("/get-subject-by/subjectId/{subjectId}")
    ApiResponse<SubjectResponse> getSubjectById(
            @PathVariable Long subjectId);

    @GetMapping("/get-all-subjects-by/subjectIds")
    ApiResponse<List<SubjectResponse>> getSubjectByIds(
            @RequestParam List<Long> ids);
}
