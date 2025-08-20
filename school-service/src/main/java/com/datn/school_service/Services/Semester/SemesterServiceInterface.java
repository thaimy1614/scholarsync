package com.datn.school_service.Services.Semester;


import com.datn.school_service.Dto.Request.Semester.AddSemesterBySchoolYearRequest;
import com.datn.school_service.Dto.Request.Semester.AddSemesterRequest;
import com.datn.school_service.Dto.Request.Semester.SearchSemesterRequest;
import com.datn.school_service.Dto.Respone.Semester.SemesterResponse;
import com.datn.school_service.Dto.Respone.Semester.SemesterResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SemesterServiceInterface {

    public SemesterResponse getSemesterById(Long semesterId);

    Page<SemesterResponse> getAll(Pageable pageable, boolean active);

    void createSemester(AddSemesterRequest addSemesterRequest);

    void updateSemester(Long id, AddSemesterRequest addSemesterRequest );

    void deleteSemester(Long id);

    List<SemesterResponse> searchSemester(SearchSemesterRequest keyword, boolean active);

    List<SemesterResponse> getSemesterBySchoolYear(Long schoolYearId);

    void addTwoSemesterBySchoolYear(Long id, AddSemesterBySchoolYearRequest addSemesterBySchoolYearRequest);

    void updateTwoSemesterBySchoolYear(Long id1,Long id2, AddSemesterBySchoolYearRequest addSemesterBySchoolYearRequest);
}
