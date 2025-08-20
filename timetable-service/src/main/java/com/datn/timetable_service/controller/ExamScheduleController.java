package com.datn.timetable_service.controller;

import com.datn.timetable_service.dto.ApiResponse;
import com.datn.timetable_service.dto.request.ExamScheduleCreationRequest;
import com.datn.timetable_service.dto.request.GenerateExamScheduleRequest;
import com.datn.timetable_service.dto.request.GetExamScheduleResponse;
import com.datn.timetable_service.model.ExamSchedule;
import com.datn.timetable_service.service.ExamScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${application.api.prefix}/exam-schedule")
public class ExamScheduleController {
    private final ExamScheduleService examScheduleService;
    @PostMapping("/generate")
    public String generateTimetable(@RequestBody GenerateExamScheduleRequest request) {
        List<ExamSchedule> timetable = examScheduleService.generateExamSchedule(request.getExamSessionId());
        return "Timetable generated successfully!";
    }

    @GetMapping("/get-by-class-and-in-30-days")
    public ApiResponse<List<GetExamScheduleResponse>> getExamSchedulesByClassId(@RequestParam Long classId) {
        return ApiResponse.<List<GetExamScheduleResponse>>builder()
                .result(examScheduleService.getExamSchedulesByClassId(classId))
                .message("Get exam schedules by class ID successfully")
                .build();
    }

    @GetMapping("/get-by-teacher-and-in-30-days")
    public ApiResponse<List<GetExamScheduleResponse>> getExamSchedulesByTeacherId(@RequestParam String teacherId) {
        return ApiResponse.<List<GetExamScheduleResponse>>builder()
                .result(examScheduleService.getExamSchedulesByTeacherId(teacherId))
                .message("Get exam schedules by teacher ID successfully")
                .build();
    }

    @GetMapping("/get-by-class-and-semester")
    public ApiResponse<List<GetExamScheduleResponse>> getExamSchedulesByClassIdAndSemesterId(
            @RequestParam Long classId,
            @RequestParam Long semesterId) {
        return ApiResponse.<List<GetExamScheduleResponse>>builder()
                .result(examScheduleService.getExamSchedulesByClassIdAndSemesterId(classId, semesterId))
                .message("Get exam schedules by class ID and semester ID successfully")
                .build();
    }

    @PostMapping("/create")
    public ApiResponse<GetExamScheduleResponse> createExamSchedule(@RequestBody ExamScheduleCreationRequest examSchedule) {
        GetExamScheduleResponse createdExamSchedule = examScheduleService.createExamSchedule(examSchedule);
        return ApiResponse.<GetExamScheduleResponse>builder()
                .result(createdExamSchedule)
                .message("Exam schedule created successfully")
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<GetExamScheduleResponse> updateExamSchedule(
            @PathVariable Long id,
            @RequestBody ExamScheduleCreationRequest examSchedule) {
        GetExamScheduleResponse updatedExamSchedule = examScheduleService.updateExamSchedule(id, examSchedule);
        return ApiResponse.<GetExamScheduleResponse>builder()
                .result(updatedExamSchedule)
                .message("Exam schedule updated successfully")
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteExamSchedule(@PathVariable Long id) {
        examScheduleService.deleteExamSchedule(id);
        return ApiResponse.<Void>builder()
                .message("Exam schedule deleted successfully")
                .build();
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExamScheduleToExcel(@RequestParam Long classId,
                                                            @RequestParam Long semesterId) {
        byte[] excelData = examScheduleService.exportExamScheduleToExcel(classId, semesterId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "timetable.xlsx");
        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);

    }

    @GetMapping("/export/pdf")
    public ResponseEntity<ByteArrayResource> exportExamScheduleToPDF(
            @RequestParam Long semesterId,
            @RequestParam Long classId
    ) throws IOException, InterruptedException {
        byte[] pdfBytes = examScheduleService.generateExamScheduleToPDF(semesterId, classId);
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                String.format("Timetable_%s_%d_Week.pdf",
                        classId != null ? classId : "All", semesterId));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(pdfBytes.length)
                .body(resource);
    }
}
