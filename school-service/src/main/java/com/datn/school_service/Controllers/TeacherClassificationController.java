package com.datn.school_service.Controllers;

import com.datn.school_service.Dto.Request.Teacherlassification.AddTeacherlassificationRequest;
import com.datn.school_service.Dto.Request.Teacherlassification.DescriptionRequest;
import com.datn.school_service.Dto.Request.Teacherlassification.SearchTeacherlassification;
import com.datn.school_service.Dto.Request.Teacherlassification.TeacherClassificationInSemesterRequest;
import com.datn.school_service.Dto.Request.ViolationType.AddViolationTypeRequest;
import com.datn.school_service.Dto.Respone.ApiResponse;
import com.datn.school_service.Dto.Respone.Teacherlassification.TeacherClassificationInSemesterResponse;
import com.datn.school_service.Dto.Respone.Teacherlassification.TeacherlassificationResponse;
import com.datn.school_service.Services.TeacherClassification.TeacherClassificationInterface;
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
@RequestMapping("${application.api.prefix}/teacher-classification")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TeacherClassificationController {

    TeacherClassificationInterface service;
//
//    @PostMapping("/create")
//    public ApiResponse<TeacherlassificationResponse> create(@RequestBody @Valid AddTeacherlassificationRequest request) {
//        return ApiResponse.<TeacherlassificationResponse>builder()
//                .result(service.createTeacherClassification(request))
//                .build();
//    }

    @PutMapping("/update/{id}")
    public ApiResponse<TeacherlassificationResponse> update(@PathVariable Long id,
                                                            @RequestBody @Valid AddTeacherlassificationRequest request) {
        return ApiResponse.<TeacherlassificationResponse>builder()
                .result(service.updateTeacherlassification(id, request))
                .build();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.deleteTeacherlassification(id);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/getById/{id}")
    public ApiResponse<TeacherlassificationResponse> getById(@PathVariable Long id) {
        return ApiResponse.<TeacherlassificationResponse>builder()
                .result(service.getTeacherlassificationById(id))
                .build();
    }

    @GetMapping("/getAll")
    public ApiResponse<Page<TeacherlassificationResponse>> getAllActive(@RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size,
                                                                        @RequestParam(defaultValue = "teacherClassificationName") String sort,
                                                                        @RequestParam(defaultValue = "asc") String direction,
                                                                        @RequestParam(defaultValue = "true") boolean active) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equalsIgnoreCase("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<TeacherlassificationResponse>>builder()
                .result(service.getAll(pageable, active))
                .build();
    }

    @PostMapping("/search")
    public ApiResponse<List<TeacherlassificationResponse>> search(@RequestBody @Valid SearchTeacherlassification request) {
        return ApiResponse.<List<TeacherlassificationResponse>>builder()
                .result(service.searchTeacherlassification(request))
                .build();
    }

    @PutMapping("/demote/{id}")
    public ApiResponse<Void> demote(@PathVariable Long id,
                                    @RequestBody @Valid DescriptionRequest request) {
        service.demoteTeacherClassification(id, request);
        return ApiResponse.<Void>builder().build();
    }
    @PutMapping("/promote/{id}")
    public ApiResponse<Void> promote(@PathVariable Long id,
                                     @RequestBody @Valid DescriptionRequest request) {
        service.promoteTeacherClassification(id, request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/get-teacher-classification-in-semester")
    public ApiResponse<TeacherClassificationInSemesterResponse> getTeacherClassificationInSemester(@RequestBody @Valid TeacherClassificationInSemesterRequest teacherClassificationInSemesterRequest) {
        return ApiResponse.<TeacherClassificationInSemesterResponse>builder()
                .result(service.getTeacherClassificationInSemester(teacherClassificationInSemesterRequest))
                .build();
    }


}
