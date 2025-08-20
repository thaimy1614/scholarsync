package com.datn.attendance_service.client;

import com.datn.attendance_service.dto.ApiResponse;
import com.datn.attendance_service.dto.subject_service.SubjectResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "subject-service", url = "${application.client.subject-service}/subject")
public interface SubjectClient {
    @GetMapping("/get-subject-by/subjectId/{subjectId}")
    ApiResponse<SubjectResponse> getSubjectById(
            @PathVariable Long subjectId);

    @GetMapping("/get-all-subjects-by/subjectIds")
    ApiResponse<List<SubjectResponse>> getSubjectByIds(
            @RequestParam List<Long> ids);
}
