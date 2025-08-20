package com.datn.school_service.Services.studentReportService;

import com.datn.school_service.Dto.Request.StudentReport.AddStudentReportRequest;
import com.datn.school_service.Dto.Request.StudentReport.PointOneTeacherWasReportOneClassRequest;
import com.datn.school_service.Dto.Request.StudentReport.TotalPointTeacherWasReportedRequest;
import com.datn.school_service.Dto.Respone.StudentReport.AddStudentReportResponse;
import com.datn.school_service.Dto.Respone.StudentReport.TotalPointTeacherWasReportedResponse;

import java.util.List;

public interface StudentReportServiceInterface {
    AddStudentReportResponse addStudentReport(AddStudentReportRequest addStudentReportRequest);
    TotalPointTeacherWasReportedResponse getSumPointTeacherWasReportByStudentInClass(PointOneTeacherWasReportOneClassRequest pointOneTeacherWasReportOneClassRequest);
    TotalPointTeacherWasReportedResponse totalPointTeacherWasReported(TotalPointTeacherWasReportedRequest totalPointTeacherWasReportedRequest);

    AddStudentReportResponse updateStudentReport(Long Evaluasionid,AddStudentReportRequest addStudentReportRequest,Long studentDetailId);
    AddStudentReportResponse getStudentReportById(Long evaluetionSessionId);
    List<AddStudentReportResponse> getStudentReportByClass(PointOneTeacherWasReportOneClassRequest pointOneTeacherWasReportOneClassRequest);
    AddStudentReportResponse updateStudentReport(Long id, AddStudentReportRequest addStudentReportRequest);

}
