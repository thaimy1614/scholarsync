package com.datn.school_service.Controllers;

import com.datn.school_service.Dto.Request.Semester.AddSemesterBySchoolYearRequest;
import com.datn.school_service.Dto.Request.Semester.AddSemesterRequest;
import com.datn.school_service.Dto.Request.Semester.SearchSemesterRequest;
import com.datn.school_service.Dto.Respone.Semester.SemesterResponse;
import com.datn.school_service.Dto.Respone.ApiResponse;
import com.datn.school_service.Dto.Respone.Semester.SemesterResponse;
import com.datn.school_service.Services.Semester.SemesterService;
import com.datn.school_service.Services.Semester.SemesterServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("${application.api.prefix}/semester")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SemesterController {
    final SemesterServiceInterface semesterService;
    @GetMapping("/get-semester-by-id/{id}")
    public ApiResponse<SemesterResponse> getSemesterById(@PathVariable Long id) {
        return ApiResponse.<SemesterResponse>builder().result(semesterService.getSemesterById(id)).build();
    }
    @GetMapping("/getAllSemesterActive")
    public ApiResponse<Page<SemesterResponse>> getAllSemestersActive(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "semesterName") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction

    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<SemesterResponse>>builder()
                .result(semesterService.getAll(pageable, true))
                .build();
    }

    @GetMapping("/getAllSemesterDelete")
    public ApiResponse<Page<SemesterResponse>> getAllSemestersDelete(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "semesterName") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction

    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<SemesterResponse>>builder()
                .result(semesterService.getAll(pageable, false))
                .build();
    }

  

    @PostMapping("/addSemester")
    public ApiResponse<Void> createSemester(@RequestBody @Valid AddSemesterRequest addSemesterRequest) {
        semesterService.createSemester(addSemesterRequest);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/editSemester/{id}")
    public ApiResponse<Void> updateSemester(
            @PathVariable Long id,
            @RequestBody @Valid  AddSemesterRequest addSemesterRequest
    ) {
        semesterService.updateSemester(id, addSemesterRequest);
        return ApiResponse.<Void>builder().build();

    }

    @DeleteMapping("/DeleteSemester/{id}")
    public ApiResponse<Void> deleteSemester(@PathVariable Long id) {
        semesterService.deleteSemester(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/searchSemester/{active}")
    public ApiResponse<List<SemesterResponse>> searchSemesters(@RequestBody @Valid SearchSemesterRequest keyword, @PathVariable boolean active) {
        return ApiResponse.<List<SemesterResponse>>builder().result(semesterService.searchSemester(keyword, active)).build();
    }

    @GetMapping("/get-semester-by-school-year/{id}")
    public ApiResponse<List<SemesterResponse>> getSemesterBySchoolYear(@PathVariable Long id) {
        return ApiResponse.<List<SemesterResponse>>builder().result(semesterService.getSemesterBySchoolYear(id)).build();
    }

    @PostMapping("/addTwoSemesterInSchoolYear/{schoolyearid}")
    @Operation(
            summary = "create semester don't need add id inside",
            description = "need add school year id in url but school year id in semester 1 and semester 2 allow null don't need add"
    )
    public ApiResponse<Void> createSemester(@PathVariable Long schoolyearid,@RequestBody @Valid AddSemesterBySchoolYearRequest addSemesterBySchoolYearRequest) {
        semesterService.addTwoSemesterBySchoolYear(schoolyearid,addSemesterBySchoolYearRequest);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/editTwoSemester")
    public ApiResponse<Void> updateTwoSemester(
            @RequestParam Long idSemester1,
            @RequestParam Long idSemester2,

            @RequestBody @Valid  AddSemesterBySchoolYearRequest addSemesterBySchoolYearRequest
    ) {
        semesterService.updateTwoSemesterBySchoolYear(idSemester1,idSemester2, addSemesterBySchoolYearRequest);
        return ApiResponse.<Void>builder().build();

    }
}
