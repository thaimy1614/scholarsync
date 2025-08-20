package com.datn.school_service.Controllers;
import com.datn.school_service.Dto.Request.StudentReport.AddStudentReportRequest;
import com.datn.school_service.Dto.Request.StudentReport.PointOneTeacherWasReportOneClassRequest;
import com.datn.school_service.Dto.Request.StudentReport.TotalPointTeacherWasReportedRequest;
import com.datn.school_service.Dto.Respone.ApiResponse;
import com.datn.school_service.Dto.Respone.NewsResponse;
import com.datn.school_service.Dto.Respone.StudentReport.AddStudentReportResponse;
import com.datn.school_service.Dto.Respone.StudentReport.TotalPointTeacherWasReportedResponse;
import com.datn.school_service.Services.studentReportService.StudentReportService;
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
@RequestMapping("${application.api.prefix}/student_report")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StudentReportController {
  final StudentReportService studentReportService;

  @PostMapping("/create-student-report")
  public ApiResponse<AddStudentReportResponse> createStudentReport(@RequestBody @Valid AddStudentReportRequest addStudentReportRequest) {
    return ApiResponse.<AddStudentReportResponse>builder()
            .result(studentReportService.addStudentReport(addStudentReportRequest))
            .build();
  }

  @PostMapping("/total-teacher-student-semester-schoolyear")
  public ApiResponse<TotalPointTeacherWasReportedResponse> total(
          @RequestBody @Valid TotalPointTeacherWasReportedRequest request) {
    return ApiResponse.<TotalPointTeacherWasReportedResponse>builder()
            .result(studentReportService.totalPointTeacherWasReported(request))
            .build();
  }

  @PostMapping("/total-point-report-teacher-in-class")
  public ApiResponse<TotalPointTeacherWasReportedResponse> totalPointReportTeacherInClass(
          @RequestBody @Valid PointOneTeacherWasReportOneClassRequest request) {
    return ApiResponse.<TotalPointTeacherWasReportedResponse>builder()
            .result(studentReportService.getSumPointTeacherWasReportByStudentInClass(request))
            .build();
  }

  @GetMapping("get-student-report-by-id/{id}")
  public ApiResponse<AddStudentReportResponse> getStudentReportById(@PathVariable Long id) {
    return ApiResponse.<AddStudentReportResponse>builder()
            .result(studentReportService.getStudentReportById(id))
            .build();
  }

}
