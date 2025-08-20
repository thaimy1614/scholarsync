package com.datn.school_service.Controllers;

import com.datn.school_service.Dto.Request.RecordCollectiveViolations.AddRecordCollectiveViolationsRequest;
import com.datn.school_service.Dto.Request.RecordCollectiveViolations.StartEndDayRequest;
import com.datn.school_service.Dto.Request.RecordCollectiveViolations.UpdateRecordCollectiveViolationsRequest;
import com.datn.school_service.Dto.Respone.ApiResponse;
import com.datn.school_service.Dto.Respone.RecordCollectiveViolations.AddRecordCollectiveViolationsResponse;
import com.datn.school_service.Dto.Respone.RecordCollectiveViolations.WeeklyViolationPointResponse;
import com.datn.school_service.Services.RecordCollectiveViolations.RecordCollectiveViolationsService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("${application.api.prefix}/record-violations-class")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RecordCollectiveViolationsController {
    private final RecordCollectiveViolationsService recordCollectiveViolationsService;
    @PostMapping("/add-record-violation-in-class")
    public ApiResponse<AddRecordCollectiveViolationsResponse> createRecordCollectiveViolations(@RequestBody @Valid AddRecordCollectiveViolationsRequest request) {
        return ApiResponse.<AddRecordCollectiveViolationsResponse>builder().result(recordCollectiveViolationsService.addRecordCollectiveViolations(request)).build();
    }
    @GetMapping("/get-record-violation-by-id/{id}")
    public ApiResponse<AddRecordCollectiveViolationsResponse> getRecordCollectiveViolationsById(@PathVariable Long id) {
        return ApiResponse.<AddRecordCollectiveViolationsResponse>builder()
                .result(recordCollectiveViolationsService.getByRecordCollectiveViolationsId(id))
                .build();
    }
    @GetMapping("/get-all-record-by-class-id/{id}")
    public ApiResponse<List<AddRecordCollectiveViolationsResponse>> getAllByClassId(@PathVariable Long id) {
        return ApiResponse.<List<AddRecordCollectiveViolationsResponse>>builder()
                .result(recordCollectiveViolationsService.getAllByRecordCollectiveViolationsByClassId(id))
                .build();
    }
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> deleteRecord(@PathVariable Long id) {
        recordCollectiveViolationsService.deleteByRecordCollectiveViolationsId(id);
        return ApiResponse.<Void>builder().build();
    }
    @GetMapping("/get-all-record-active")
    public ApiResponse<Page<AddRecordCollectiveViolationsResponse>> getAllRecordCollectiveViolationsActive(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "absentCount") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction

    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<AddRecordCollectiveViolationsResponse>>builder()
                .result(recordCollectiveViolationsService.getAll(pageable, true))
                .build();
    }
    @GetMapping("/getAllViolationCollectiveInADay")
    public ApiResponse<Page<AddRecordCollectiveViolationsResponse>> getAllViolationCollectiveInADay(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "absentCount") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction
    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equalsIgnoreCase("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);

        if (date == null) {
            date = LocalDate.now();
        }

        return ApiResponse.<Page<AddRecordCollectiveViolationsResponse>>builder()
                .result(recordCollectiveViolationsService.getAllViolationCollectiveInADay(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()), pageable))
                .build();
    }

    @GetMapping("/get-all-record-delete")
    public ApiResponse<Page<AddRecordCollectiveViolationsResponse>> getAllRecordCollectiveViolationsDelete(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "absentCount") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction

    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<AddRecordCollectiveViolationsResponse>>builder()
                .result(recordCollectiveViolationsService.getAll(pageable, false))
                .build();
    }
    @PutMapping("/update-record/{id}")
    public ApiResponse<Void> updateClass(@PathVariable Long id, @RequestBody UpdateRecordCollectiveViolationsRequest request) {

              recordCollectiveViolationsService.updateByRecordCollectiveViolations(id, request);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/get-record-violation-of-class-by-date-for-teacher")
    public ApiResponse<AddRecordCollectiveViolationsResponse> getRecordViolationForTeacherByDate(
            @RequestParam Long classId,

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dateParam) {

        AddRecordCollectiveViolationsResponse result = recordCollectiveViolationsService
                .getByRecordCollectiveViolationsForTeacher(classId, dateParam);

        return ApiResponse.<AddRecordCollectiveViolationsResponse>builder().result(result).build();
    }

    @GetMapping("/weekly-violation-simple/{classId}")
    public ApiResponse<List<WeeklyViolationPointResponse>> getWeeklyViolationSimple(@PathVariable Long classId) {
        List<WeeklyViolationPointResponse> result =
                recordCollectiveViolationsService.getWeeklyViolationOfClassSimple(classId);
        return ApiResponse.<List<WeeklyViolationPointResponse>>builder()
                .result(result)
                .build();
    }
    @GetMapping("/get-all-class-weekly-violations/{schoolYearId}")
    public ApiResponse<List<WeeklyViolationPointResponse>> getAllClassWeeklyViolations(
            @PathVariable Long schoolYearId) {
        List<WeeklyViolationPointResponse> result =
                recordCollectiveViolationsService.getAllClassWeeklyViolationsBySchoolYear(schoolYearId);
        return ApiResponse.<List<WeeklyViolationPointResponse>>builder()
                .result(result)
                .build();
    }
    @GetMapping("/weekly-ranked")
    public ApiResponse<Page<WeeklyViolationPointResponse>> getWeeklyRanked(
            @RequestParam(value = "startDate", defaultValue = "2024-01-01") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(value = "endDate", defaultValue = "2024-12-30") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
//            @RequestParam(value = "sort", defaultValue = "classPoint") String sort,
//            @RequestParam(value = "direction", defaultValue = "asc") String direction
    ) {
        String sort ="classPoint";
        String direction="asc";
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equalsIgnoreCase("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);

        StartEndDayRequest request = new StartEndDayRequest(startDate, endDate);

        Page<WeeklyViolationPointResponse> result =
                recordCollectiveViolationsService.getAllClassWeeklyViolationsByWWeek(request, pageable,sort,direction);

        return ApiResponse.<Page<WeeklyViolationPointResponse>>builder()
                .result(result)
                .build();
    }



}
