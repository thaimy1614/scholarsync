package com.datn.attendance_service.controller;

import com.datn.attendance_service.dto.ApiResponse;
import com.datn.attendance_service.dto.request.RecordStudentAttendanceRequest;
import com.datn.attendance_service.dto.response.AttendanceSummaryResponse;
import com.datn.attendance_service.dto.response.RecordStudentAttendanceResponse;
import com.datn.attendance_service.dto.response.TeacherAttendanceHistoryResponse;
import com.datn.attendance_service.dto.response.TeacherAttendanceResponse;
import com.datn.attendance_service.model.*;
import com.datn.attendance_service.service.AttendanceService;
import com.datn.attendance_service.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("${application.api.prefix}")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final ReportService reportService;

    @PostMapping("/take-attendance/student")
    public ApiResponse<RecordStudentAttendanceResponse> recordStudentAttendance(@RequestBody RecordStudentAttendanceRequest record) {
        return ApiResponse.<RecordStudentAttendanceResponse>builder()
                .result(attendanceService.recordStudentAttendance(record))
                .build();
    }

    @PostMapping("/take-attendance/student/for-admin")
    public ApiResponse<RecordStudentAttendanceResponse> recordStudentAttendanceForAdmin(@RequestBody RecordStudentAttendanceRequest record) {
        return ApiResponse.<RecordStudentAttendanceResponse>builder()
                .result(attendanceService.recordStudentAttendanceForAdmin(record))
                .build();
    }

    @PostMapping("/take-attendance/student/bulk")
    public ApiResponse<List<RecordStudentAttendanceResponse>> recordBulkAttendance(@RequestBody List<RecordStudentAttendanceRequest> records) {
        return ApiResponse.<List<RecordStudentAttendanceResponse>>builder()
                .result(attendanceService.recordBulkAttendance(records))
                .build();
    }

    @PutMapping("/check-attendance/student/{id}/correct")
    public ResponseEntity<RecordStudentAttendanceResponse> correctAttendance(@PathVariable Long id,
                                                              @RequestBody AttendanceRecord.AttendanceStatus status,
                                                              @RequestParam Long updatedBy) {
        return ResponseEntity.ok(attendanceService.correctAttendance(id, status, updatedBy));
    }

    @PostMapping("/take-attendance/teacher")
    public ResponseEntity<TeacherAttendance> recordTeacherAttendance(@RequestBody TeacherAttendance record) {
        return ResponseEntity.ok(attendanceService.recordTeacherAttendance(record));
    }

    @GetMapping("/history/student/{studentId}")
    public ResponseEntity<List<AttendanceRecord>> getStudentAttendanceHistory(
            @PathVariable String studentId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(attendanceService.getStudentAttendanceHistory(studentId, startDate, endDate));
    }

    @GetMapping("/history/student/by-subject-and-date")
    public ApiResponse<List<RecordStudentAttendanceResponse>> getStudentAttendanceHistory(
            @RequestParam String studentId,
            @RequestParam Long subjectId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ApiResponse.<List<RecordStudentAttendanceResponse>>builder()
                .result(attendanceService.getStudentAttendanceHistoryBySubject(studentId, subjectId, startDate, endDate))
                .build();
    }

    @GetMapping("/get-attendance/class")
    public ApiResponse<List<RecordStudentAttendanceResponse>> getClassAttendance(
            @RequestParam Long classId,
            @RequestParam LocalDate date) {
        return ApiResponse.<List<RecordStudentAttendanceResponse>>builder()
                .result(attendanceService.getClassAttendance(classId, date))
                .build();
    }

    @GetMapping("/get-attendance/class-subject")
    public ApiResponse<List<RecordStudentAttendanceResponse>> getClassAttendance(
            @RequestParam Long classId,
            @RequestParam Long subjectId,
            @RequestParam LocalDate date) {
        return ApiResponse.<List<RecordStudentAttendanceResponse>>builder()
                .result(attendanceService.getClassAttendanceBySubject(classId, subjectId, date))
                .build();
    }

    @GetMapping("/history/teacher/{teacherId}")
    public ApiResponse<List<TeacherAttendanceHistoryResponse>> getTeacherAttendanceHistory(
            @PathVariable String teacherId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ApiResponse.<List<TeacherAttendanceHistoryResponse>>builder()
                .result(attendanceService.getTeacherAttendanceHistory(teacherId, startDate, endDate))
                .build();
    }

    @GetMapping("/summary")
    public ApiResponse<AttendanceSummaryResponse> generateAttendanceSummary(
            @RequestParam String studentId,
            @RequestParam AttendanceSummary.PeriodType periodType,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ApiResponse.<AttendanceSummaryResponse>builder()
                .result(attendanceService.generateAttendanceSummary(studentId, periodType, startDate, endDate))
                .build();
    }

    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> generateAttendanceReportPdf(
            @RequestParam String studentId,
            @RequestParam AttendanceSummary.PeriodType periodType,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        AttendanceSummaryResponse summary = attendanceService.generateAttendanceSummary(studentId, periodType, startDate, endDate);
        byte[] pdf = reportService.generateAttendanceReportPdf(summary);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/report/csv")
    public ResponseEntity<byte[]> generateAttendanceReportCsv(
            @RequestParam String studentId,
            @RequestParam AttendanceSummary.PeriodType periodType,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        AttendanceSummaryResponse summary = attendanceService.generateAttendanceSummary(studentId, periodType, startDate, endDate);
        byte[] csv = reportService.generateAttendanceReportCsv(summary);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_report.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }

    @GetMapping("/by-timetable-id/{timetableId}")
    public ApiResponse<List<RecordStudentAttendanceResponse>> getAttendanceByTimetableId(
            @PathVariable Long timetableId) {
        List<RecordStudentAttendanceResponse> attendanceResponses = attendanceService.getAttendanceByTimetableId(timetableId);
        return ApiResponse.<List<RecordStudentAttendanceResponse>>builder()
                .result(attendanceResponses)
                .build();
    }

    @PostMapping("/init-by-all-timetable-slots")
    public ApiResponse<Void> initAttendanceByAllTimetableSlots() {
        attendanceService.initAttendanceByAllTimetableSlots();
        return ApiResponse.<Void>builder()
                .message("Attendance initialized successfully for all timetable slots")
                .build();
    }
}