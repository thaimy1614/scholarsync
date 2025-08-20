package com.datn.timetable_service.controller;

import com.datn.timetable_service.dto.ApiResponse;
import com.datn.timetable_service.dto.request.ExamSessionRequest;
import com.datn.timetable_service.dto.response.ExamSessionResponse;
import com.datn.timetable_service.service.ExamSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${application.api.prefix}/exam-sessions")
@RequiredArgsConstructor
public class ExamSessionController {
    private final ExamSessionService examSessionService;

    @PostMapping
    public ApiResponse<ExamSessionResponse> createExamSession(@Valid @RequestBody ExamSessionRequest request) {
        return ApiResponse.<ExamSessionResponse>builder()
                .message("Exam session created successfully")
                .result(examSessionService.createExamSession(request))
                .build();
    }

    @PutMapping("/{examSessionId}")
    public ApiResponse<ExamSessionResponse> updateExamSession(
            @PathVariable Long examSessionId,
            @Valid @RequestBody ExamSessionRequest request) {
        return ApiResponse.<ExamSessionResponse>builder()
                .message("Exam session updated successfully")
                .result(examSessionService.updateExamSession(examSessionId, request))
                .build();
    }

    @DeleteMapping("/{examSessionId}")
    public ApiResponse<Void> deleteExamSession(@PathVariable Long examSessionId) {
        examSessionService.deleteExamSession(examSessionId);
        return ApiResponse.<Void>builder()
                .message("Exam session deleted successfully")
                .build();
    }

    @GetMapping("/{examSessionId}")
    public ApiResponse<ExamSessionResponse> getExamSessionById(@PathVariable Long examSessionId) {
        return ApiResponse.<ExamSessionResponse>builder()
                .message("Exam session retrieved successfully")
                .result(examSessionService.getExamSessionById(examSessionId))
                .build();
    }

    @GetMapping
    public ApiResponse<List<ExamSessionResponse>> getAllExamSessions(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long schoolYearId) {
        return ApiResponse.<List<ExamSessionResponse>>builder()
                .message("Exam sessions retrieved successfully")
                .result(examSessionService.getAllExamSessions(semesterId, schoolYearId))
                .build();
    }
}
