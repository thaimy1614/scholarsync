package com.datn.resource_service.client;

import com.amazonaws.services.accessanalyzer.model.CheckAccessNotGrantedResult;
import com.datn.resource_service.dto.ApiResponse;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "schoolClient", url = "${client.school.url}/school")
public interface SchoolClient {
    @GetMapping("/school-year/get-school-years/by-ids")
    ApiResponse<List<SchoolYearResponse>> getSchoolYearsByIds(@RequestParam List<Long> ids);

    @GetMapping("/school-year/getSchoolYearById/{id}")
    ApiResponse<SchoolYearResponse> getSchoolYearById(@PathVariable Long id);

    @GetMapping("/grade/get-grade-by-ids")
    ApiResponse<List<GradeResponse>> getGradesByIds(@RequestParam List<Long> gradeIds);

    @GetMapping("/grade/getGradeById/{id}")
    ApiResponse<GradeResponse> getGradeById(@PathVariable Long id);

    @Data
    static class SchoolYearResponse{
        private Long schoolYearId;
        private String schoolYear;
    }

    @Data
    static class GradeResponse {
        private Long gradeId;
        private String gradeName;
    }
}
