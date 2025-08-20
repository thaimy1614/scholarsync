package com.datn.school_service.Services.SchoolYear;

import com.datn.school_service.Dto.Request.SchoolYear.AddSchoolYearRequest;

import com.datn.school_service.Dto.Request.SchoolYear.SearchSchoolYearRequest;
import com.datn.school_service.Dto.Respone.SchoolYear.GetDayOfWeekResponse;
import com.datn.school_service.Dto.Respone.SchoolYear.SchoolYearResponse;
import com.datn.school_service.Dto.Respone.SchoolYear.SemesterBySchoolYearResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SchoolYearServiceInterface {
    Page<SchoolYearResponse> getAll(Pageable pageable, boolean active);

    SchoolYearResponse getSchoolYearById(Long id);

    void createSchoolYear(AddSchoolYearRequest addSchoolYearRequest);

    void updateSchoolYear(Long id, AddSchoolYearRequest addSchoolYearRequest );

    void deleteSchoolYear(Long id);

    List<SchoolYearResponse> searchSchoolYear(SearchSchoolYearRequest keyword, boolean active);

    SemesterBySchoolYearResponse getAllSemesterBySchoolYearId(Long id);


    Page<SemesterBySchoolYearResponse> getAllSchoolYearWithSemesterById(Pageable pageable, boolean active);

    List<SchoolYearResponse> getSchoolYearByIds(List<Long> ids);

    List<GetDayOfWeekResponse> getAllWeeksBySchoolYear(Long schoolYearId);
}
