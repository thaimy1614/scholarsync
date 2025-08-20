package com.datn.school_service.Services.RecordCollectiveViolations;

import com.datn.school_service.Dto.Request.RecordCollectiveViolations.AddRecordCollectiveViolationsRequest;
import com.datn.school_service.Dto.Request.RecordCollectiveViolations.StartEndDayRequest;
import com.datn.school_service.Dto.Request.RecordCollectiveViolations.UpdateRecordCollectiveViolationsRequest;
import com.datn.school_service.Dto.Respone.RecordCollectiveViolations.AddRecordCollectiveViolationsResponse;
import com.datn.school_service.Dto.Respone.RecordCollectiveViolations.WeeklyViolationPointResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

public interface RecordCollectiveViolationsInterface {
    AddRecordCollectiveViolationsResponse addRecordCollectiveViolations(AddRecordCollectiveViolationsRequest request);
    AddRecordCollectiveViolationsResponse getByRecordCollectiveViolationsId(Long id);
    List<AddRecordCollectiveViolationsResponse> getAllByRecordCollectiveViolationsByClassId(Long id);
    void deleteByRecordCollectiveViolationsId(Long id);
    void updateByRecordCollectiveViolations(Long id, UpdateRecordCollectiveViolationsRequest request);

    Page<AddRecordCollectiveViolationsResponse> getAll(Pageable pageable, boolean active);

    AddRecordCollectiveViolationsResponse getByRecordCollectiveViolationsForTeacher(Long id, Date date);

    double sumViolationInOneRecordCollectiveViolations(Long id);

    List<WeeklyViolationPointResponse> getWeeklyViolationOfClassSimple(Long classId);
    //List<OneCollectiveViolationInSchoolYearResponse> getClassSchoolYearViolation(OneCollectiveViolationInSchoolYearRequest oneCollectiveViolationInSchoolYearRequest);
    List<WeeklyViolationPointResponse> getAllClassWeeklyViolationsBySchoolYear(Long schoolYearId);

    Page<WeeklyViolationPointResponse> getAllClassWeeklyViolationsByWWeek(StartEndDayRequest request, Pageable pageable, String sort, String direction);

    Page<AddRecordCollectiveViolationsResponse> getAllViolationCollectiveInADay(Date date, Pageable pageable);

}
