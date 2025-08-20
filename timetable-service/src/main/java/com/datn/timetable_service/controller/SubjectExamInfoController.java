package com.datn.timetable_service.controller;

import com.datn.timetable_service.dto.ApiResponse;
import com.datn.timetable_service.dto.request.SubjectExamInfoRequest;
import com.datn.timetable_service.dto.response.SubjectExamInfoResponse;
import com.datn.timetable_service.service.SubjectExamInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${application.api.prefix}/subject-exam-info")
@RequiredArgsConstructor
public class SubjectExamInfoController {
    private final SubjectExamInfoService subjectExamInfoService;

    @PostMapping
    public ApiResponse<SubjectExamInfoResponse> createSubjectExamInfo(@Valid @RequestBody SubjectExamInfoRequest request) {
        return ApiResponse.<SubjectExamInfoResponse>builder()
                .message("Subject exam info created successfully")
                .result(subjectExamInfoService.createSubjectExamInfo(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<SubjectExamInfoResponse> updateSubjectExamInfo(
            @PathVariable Long id,
            @Valid @RequestBody SubjectExamInfoRequest request) {
        return ApiResponse.<SubjectExamInfoResponse>builder()
                .message("Subject exam info updated successfully")
                .result(subjectExamInfoService.updateSubjectExamInfo(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSubjectExamInfo(@PathVariable Long id) {
        subjectExamInfoService.deleteSubjectExamInfo(id);
        return ApiResponse.<Void>builder()
                .message("Subject exam info deleted successfully")
                .build();
    }

    @GetMapping("/subject/{subjectId}")
    public ApiResponse<SubjectExamInfoResponse> getSubjectExamInfoBySubjectId(@PathVariable Long subjectId) {
        return ApiResponse.<SubjectExamInfoResponse>builder()
                .message("Subject exam info retrieved successfully")
                .result(subjectExamInfoService.getSubjectExamInfoBySubjectId(subjectId))
                .build();
    }

    @GetMapping
    public ApiResponse<List<SubjectExamInfoResponse>> getAllSubjectExamInfo() {
        return ApiResponse.<List<SubjectExamInfoResponse>>builder()
                .message("Subject exam info list retrieved successfully")
                .result(subjectExamInfoService.getAllSubjectExamInfo())
                .build();
    }
}