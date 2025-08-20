package com.datn.resource_service.client;

import com.datn.resource_service.dto.ApiResponse;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "subjectClient", url = "${client.subject.url}/subject")
public interface SubjectClient {
    @GetMapping("/get-subjects-by/schoolYearId/{schoolYearId}")
    ApiResponse<List<SubjectResponse>> getAllSubjectBySchoolYearId(@PathVariable Long schoolYearId);

    @GetMapping("/get-all-subjects-by/subjectIds")
    ApiResponse<List<SubjectResponse>> getSubjectByIds(
            @RequestParam List<Long> ids);

    @GetMapping("/get-subject-by/subjectId/{subjectId}")
    ApiResponse<SubjectResponse> getSubjectById(@PathVariable Long subjectId);

    @Data
    static class SubjectResponse {
        private String id;
        private String name;
    }
}
