package com.datn.school_service.Controllers;

import com.datn.school_service.Dto.Request.SchoolYear.AddSchoolYearRequest;
import com.datn.school_service.Dto.Request.SchoolYear.SearchSchoolYearRequest;
import com.datn.school_service.Dto.Respone.SchoolYear.GetDayOfWeekResponse;
import com.datn.school_service.Dto.Respone.SchoolYear.SchoolYearResponse;
import com.datn.school_service.Dto.Respone.ApiResponse;
import com.datn.school_service.Dto.Respone.SchoolYear.SemesterBySchoolYearResponse;
import com.datn.school_service.Services.SchoolYear.SchoolYearServiceInterface;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${application.api.prefix}/school-year")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SchoolYearController {
    private final SchoolYearServiceInterface schoolYearService;

    @GetMapping("/getAllSchoolYearActive")
    public ApiResponse<Page<SchoolYearResponse>> getAllSchoolYearsActive(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "schoolYear") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction

    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<SchoolYearResponse>>builder()
                .result(schoolYearService.getAll(pageable, true))
                .build();
    }

    @GetMapping("/getAllSchoolYearDelete")
    public ApiResponse<Page<SchoolYearResponse>> getAllSchoolYearsDelete(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "schoolYear") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction

    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<SchoolYearResponse>>builder()
                .result(schoolYearService.getAll(pageable, false))
                .build();
    }

    @GetMapping("/getSchoolYearById/{id}")
    public ApiResponse<SchoolYearResponse> getSchoolYearById(@PathVariable Long id) {
        return ApiResponse.<SchoolYearResponse>builder().result(schoolYearService.getSchoolYearById(id)).build();
    }

    @GetMapping("/get-school-years/by-ids")
    public ApiResponse<List<SchoolYearResponse>> getSchoolYearsByIds(@RequestParam List<Long> ids) {
        return ApiResponse.<List<SchoolYearResponse>>builder().result(schoolYearService.getSchoolYearByIds(ids)).build();
    }

    @PostMapping("/addSchoolYear")
    public ApiResponse<Void> createSchoolYear(@RequestBody @Valid AddSchoolYearRequest addSchoolYearRequest) {
        schoolYearService.createSchoolYear(addSchoolYearRequest);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/editSchoolYear/{id}")
    public ApiResponse<Void> updateSchoolYear(
            @PathVariable Long id,
            @RequestBody @Valid  AddSchoolYearRequest addSchoolYearRequest
    ) {
        schoolYearService.updateSchoolYear(id, addSchoolYearRequest);
        return ApiResponse.<Void>builder().build();

    }

    @DeleteMapping("/DeleteSchoolYear/{id}")
    public ApiResponse<Void> deleteSchoolYear(@PathVariable Long id) {
        schoolYearService.deleteSchoolYear(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/searchSchoolYear/{active}")
    public ApiResponse<List<SchoolYearResponse>> searchSchoolYears(@RequestBody @Valid SearchSchoolYearRequest keyword, @PathVariable boolean active) {
        return ApiResponse.<List<SchoolYearResponse>>builder().result(schoolYearService.searchSchoolYear(keyword, active)).build();
    }

    @GetMapping("/getSchoolYearWithSemesterById/{id}")
    public ApiResponse<SemesterBySchoolYearResponse> getSchoolYearWithSemesterById(@PathVariable Long id) {
        return ApiResponse.<SemesterBySchoolYearResponse>builder().result(schoolYearService.getAllSemesterBySchoolYearId(id)).build();
    }

    @GetMapping("/getAllSchoolYearWithSemesterActive")
    public ApiResponse<Page<SemesterBySchoolYearResponse>> getAllSchoolYearsWithSemesterActive(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "schoolYear") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            @RequestParam boolean active

    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<SemesterBySchoolYearResponse>>builder()
                .result(schoolYearService.getAllSchoolYearWithSemesterById(pageable, active))
                .build();
    }
    @GetMapping("/getDayOfWeekBySchoolYearId")
    public ApiResponse<List<GetDayOfWeekResponse>> getDayOfWeekBySchoolYearId(@RequestParam Long id) {
        return ApiResponse.<List<GetDayOfWeekResponse>>builder()
                .result(schoolYearService.getAllWeeksBySchoolYear(id))
                .build();
    }

}
