package com.datn.school_service.Controllers;


import com.datn.school_service.Dto.Request.Grade.AddGradeRequest;
import com.datn.school_service.Dto.Respone.ApiResponse;
import com.datn.school_service.Dto.Respone.Grade.GradeResponse;
import com.datn.school_service.Services.Grade.GradeServiceInterface;
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
@RequestMapping("${application.api.prefix}/grade")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class GradeController {
    private final GradeServiceInterface gradeService;

    @GetMapping("/getAllGradeActive")
    public ApiResponse<Page<GradeResponse>> getAllGradesActive(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "gradeName") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction

    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<GradeResponse>>builder()
                .result(gradeService.getAll(pageable, true))
                .build();
    }

    @GetMapping("/getAllGradeDelete")
    public ApiResponse<Page<GradeResponse>> getAllGradesDelete(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "gradeName") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction

    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<GradeResponse>>builder()
                .result(gradeService.getAll(pageable, false))
                .build();
    }

    @GetMapping("/getGradeById/{id}")
    public ApiResponse<GradeResponse> getGradeById(@PathVariable Long id) {
        return ApiResponse.<GradeResponse>builder().result(gradeService.getGradeById(id)).build();
    }

    @PostMapping("/addGrade")
    public ApiResponse<Void> createGrade(@RequestBody @Valid AddGradeRequest addGradeRequest) {
        gradeService.createGrade(addGradeRequest);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/editGrade/{id}")
    public ApiResponse<Void> updateGrade(
            @PathVariable Long id,
            @RequestBody @Valid AddGradeRequest addGradeRequest
    ) {
        gradeService.updateGrade(id, addGradeRequest);
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping("/DeleteGrade/{id}")
    public ApiResponse<Void> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/searchGrade/{active}")
    public ApiResponse<List<GradeResponse>> searchGrades(@RequestBody @Valid AddGradeRequest keyword, @PathVariable boolean active) {
        return ApiResponse.<List<GradeResponse>>builder().result(gradeService.searchGrade(keyword, active)).build();
    }

    @GetMapping("/get-grade-by-ids")
    public ApiResponse<List<GradeResponse>> getGradesByIds(@RequestParam List<Long> gradeIds) {
        return ApiResponse.<List<GradeResponse>>builder()
                .result(gradeService.getGradesByIds(gradeIds))
                .build();
    }
}
