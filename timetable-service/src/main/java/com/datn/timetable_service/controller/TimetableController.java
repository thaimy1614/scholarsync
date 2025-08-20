package com.datn.timetable_service.controller;

import com.datn.timetable_service.dto.ApiResponse;
import com.datn.timetable_service.dto.request.*;
import com.datn.timetable_service.dto.response.SlotDetailResponse;
import com.datn.timetable_service.dto.response.SlotDetailResponseV2;
import com.datn.timetable_service.dto.response.TimetableResponse;
import com.datn.timetable_service.model.Timetable;
import com.datn.timetable_service.service.MainTimetableService;
import com.datn.timetable_service.service.TimetableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${application.api.prefix}")
@RequiredArgsConstructor
public class TimetableController {
    private final TimetableService timetableService;
    private final MainTimetableService mainTimetableService;

    @PostMapping("/generate")
    public String generateTimetable(@RequestBody GenerateTimetableRequest request) {
        List<Timetable> timetable = timetableService.generateTimetable(request.getSemesterId(),
                request.getSchoolYearId(), request.getWeekNumber());
        return "Timetable generated successfully!";
    }

    @PostMapping
    public ApiResponse<TimetableResponse> createTimetableSlot(@Valid @RequestBody TimetableCreateRequest dto) {
        return ApiResponse.<TimetableResponse>builder()
                .code(1000)
                .message("Timetable slot created successfully")
                .result(mainTimetableService.createTimetableSlot(dto))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<TimetableResponse> updateTimetableSlot(@PathVariable Long id,
                                                              @Valid @RequestBody TimetableUpdateRequest dto) {
        return ApiResponse.<TimetableResponse>builder()
                .code(1000)
                .message("Timetable slot updated successfully")
                .result(mainTimetableService.updateTimetableSlot(id, dto))
                .build();
    }

    @DeleteMapping("/{id}/soft")
    public ApiResponse<Void> softDeleteTimetableSlot(@PathVariable Long id) {
        mainTimetableService.softDeleteTimetableSlot(id);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Timetable slot soft deleted successfully")
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTimetableSlot(@PathVariable Long id) {
        mainTimetableService.deleteTimetableSlot(id);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Timetable slot deleted successfully")
                .build();
    }

    @PutMapping("/{id}/restore")
    public ApiResponse<Void> restoreTimetableSlot(@PathVariable Long id) {
        mainTimetableService.restoreTimetableSlot(id);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Timetable slot restored successfully")
                .build();
    }

    @DeleteMapping("/{id}/permanent")
    public ApiResponse<Void> permanentlyDeleteTimetableSlot(@PathVariable Long id) {
        mainTimetableService.permanentlyDeleteTimetableSlot(id);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Timetable slot permanently deleted successfully")
                .build();
    }

    @GetMapping("/class")
    public ApiResponse<List<TimetableResponse>> getClassSchedule(
            @RequestParam Long classId,
            @RequestParam Long semesterId,
            @RequestParam int week) {
        return ApiResponse.<List<TimetableResponse>>builder()
                .code(1000)
                .message("Class schedule retrieved successfully")
                .result(mainTimetableService.getClassSchedule(classId, semesterId, week))
                .build();
    }

    @GetMapping("/{id}/full-details")
    public ApiResponse<SlotDetailResponse> getSlotDetails(@PathVariable Long id) {
        return ApiResponse.<SlotDetailResponse>builder()
                .code(1000)
                .message("Slot details retrieved successfully")
                .result(mainTimetableService.getSlotDetails(id))
                .build();
    }

    @GetMapping("/{id}/details-for-other-services")
    public ApiResponse<SlotDetailResponseV2> getSlotDetailsForOtherServices(@PathVariable Long id) {
        return ApiResponse.<SlotDetailResponseV2>builder()
                .code(1000)
                .message("Slot details retrieved successfully")
                .result(mainTimetableService.getSlotDetailsV2(id))
                .build();
    }

    @GetMapping("/details-for-other-services/by-ids")
    public ApiResponse<List<SlotDetailResponseV2>> getSlotDetailsForOtherServicesByIds(@RequestParam List<Long> ids) {
        return ApiResponse.<List<SlotDetailResponseV2>>builder()
                .code(1000)
                .message("Slot details retrieved successfully")
                .result(mainTimetableService.getSlotDetailsByIdsV2(ids))
                .build();
    }

    @GetMapping("/get-all")
    public ApiResponse<List<SlotDetailResponseV2>> getAllTimetableSlots() {
        return ApiResponse.<List<SlotDetailResponseV2>>builder()
                .code(1000)
                .message("All timetable slots retrieved successfully")
                .result(mainTimetableService.getAllTimetableSlots())
                .build();
    }

    @GetMapping("/teacher")
    public ApiResponse<List<TimetableResponse>> getTeacherSchedule(
            @RequestParam String teacherId,
            @RequestParam Long semesterId,
            @RequestParam int week) {
        return ApiResponse.<List<TimetableResponse>>builder()
                .code(1000)
                .message("Teacher schedule retrieved successfully")
                .result(mainTimetableService.getTeacherSchedule(teacherId, semesterId, week))
                .build();
    }

    @GetMapping
    public ApiResponse<List<TimetableResponse>> getTimetableByFilters(
            @RequestParam Long semesterId,
            @RequestParam(required = false) Integer week,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String teacherId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.<List<TimetableResponse>>builder()
                .code(1000)
                .message("Timetable retrieved successfully")
                .result(mainTimetableService.getTimetableByFilters(semesterId, week, classId, teacherId, startDate, endDate))
                .build();
    }

    @PostMapping("/bulk")
    public ApiResponse<List<TimetableResponse>> bulkCreateTimetableSlots(@Valid @RequestBody List<TimetableCreateRequest> dtos) {
        return ApiResponse.<List<TimetableResponse>>builder()
                .code(1000)
                .message("Bulk timetable slots created successfully")
                .result(mainTimetableService.bulkCreateTimetableSlots(dtos))
                .build();
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Long>> getTimetableSummary(
            @RequestParam Long semesterId,
            @RequestParam int week) {
        return ApiResponse.<Map<String, Long>>builder()
                .code(1000)
                .message("Timetable summary retrieved successfully")
                .result(mainTimetableService.getTimetableSummary(semesterId, week))
                .build();
    }

    @PostMapping("/swap")
    public ApiResponse<Void> swapTimetableSlots(@Valid @RequestBody SlotSwapRequest dto) {
        mainTimetableService.swapTimetableSlots(dto);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Timetable slots swapped successfully")
                .build();
    }

    @PostMapping("/clone")
    public ApiResponse<List<TimetableResponse>> cloneTimetable(@Valid @RequestBody TimetableCloneRequest dto) {
        return ApiResponse.<List<TimetableResponse>>builder()
                .code(1000)
                .message("Timetable cloned successfully")
                .result(mainTimetableService.cloneTimetable(dto))
                .build();
    }


    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportTimetableToExcel(
            @RequestParam Long semesterId,
            @RequestParam int week,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String teacherId) {
        byte[] excelData = mainTimetableService.exportTimetableToExcel(semesterId, week, classId, teacherId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "timetable.xlsx");
        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<ByteArrayResource> exportTimetableToPDF(
            @RequestParam Long semesterId,
            @RequestParam int week,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String teacherId) throws IOException, InterruptedException {

        byte[] pdfBytes = mainTimetableService.generateTimetablePDF(semesterId, week, classId, teacherId);
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                String.format("Timetable_%s_%d_Week%d.pdf",
                        classId != null ? classId : "All", semesterId, week));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(pdfBytes.length)
                .body(resource);

    }
}