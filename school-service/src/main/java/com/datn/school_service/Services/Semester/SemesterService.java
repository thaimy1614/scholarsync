package com.datn.school_service.Services.Semester;

import com.datn.school_service.Dto.Request.Semester.AddSemesterBySchoolYearRequest;
import com.datn.school_service.Dto.Request.Semester.AddSemesterRequest;
import com.datn.school_service.Dto.Request.Semester.SearchSemesterRequest;
import com.datn.school_service.Dto.Respone.Semester.SemesterResponse;
import com.datn.school_service.Exceptions.AppException;
import com.datn.school_service.Exceptions.ErrorCode;
import com.datn.school_service.Mapper.SemesterMapper;
import com.datn.school_service.Models.SchoolYear;
import com.datn.school_service.Models.Semester;
import com.datn.school_service.Repository.SchoolRepository;
import com.datn.school_service.Repository.SchoolYearRepository;
import com.datn.school_service.Repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SemesterService implements SemesterServiceInterface{
final SemesterRepository semesterRepository;
final SemesterMapper semesterMapper;
    final SchoolYearRepository yearRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolYearRepository schoolYearRepository;

    @Override
    public SemesterResponse getSemesterById(Long semesterId) {

        Semester semester = semesterRepository.findById(semesterId).orElseThrow(() -> new AppException(ErrorCode.SEMESTER_NOT_EXIT));

        return semesterMapper.toSemesterResponse(semester);
    }

    @Override
    public Page<SemesterResponse> getAll(Pageable pageable, boolean active) {
        Page<Semester> semesterPage;
        if (active) {
            semesterPage = semesterRepository.findAllByIsActiveTrue(pageable);
        } else {
            semesterPage = semesterRepository.findAllByIsActiveFalse(pageable);
        }

        return semesterPage.map(semesterMapper::toSemesterResponse);

    }


    @Override
    public void createSemester(AddSemesterRequest addSemesterRequest) {

        if (addSemesterRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        boolean existsSemester = semesterRepository.existsBySemesterNameAndAndSchoolYear_SchoolYearId(addSemesterRequest.getSemesterName(), addSemesterRequest.getSchoolYearId());
        if (existsSemester) {
            throw new AppException(ErrorCode.SEMESTER_ALREADY_EXIT);
        }
        SchoolYear schoolYear = schoolYearRepository.findById(addSemesterRequest.getSchoolYearId()).orElseThrow(() -> new AppException(ErrorCode.SCHOOL_YEAR_NOT_EXIT));
        Semester semester = semesterMapper.toSemester(addSemesterRequest);
        semester.setSchoolYear(schoolYear);
        semesterRepository.save(semester);

    }


    @Override
    public void updateSemester(Long idQues, AddSemesterRequest addSemesterRequest) {
        if (idQues == null || addSemesterRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        Semester existingSemester = semesterRepository.findById(idQues)
                .orElseThrow(() -> new AppException(ErrorCode.SEMESTER_NOT_FOUND));

        String semesterName = addSemesterRequest.getSemesterName();
        Long schoolYearId = addSemesterRequest.getSchoolYearId();
        SchoolYear schoolYear = schoolYearRepository.findById(addSemesterRequest.getSchoolYearId()).orElseThrow(() -> new AppException(ErrorCode.SCHOOL_YEAR_NOT_EXIT));

        if (!semesterName.equalsIgnoreCase(existingSemester.getSemesterName()) || schoolYearId != existingSemester.getSchoolYear().getSchoolYearId()) {

            if (semesterRepository.existsBySemesterNameAndAndSchoolYear_SchoolYearId(semesterName, schoolYearId)) {
                throw new AppException(ErrorCode.SEMESTER_ALREADY_EXIT);
            }
        }

        semesterMapper.updateSemester(existingSemester, addSemesterRequest);
        existingSemester.setSchoolYear(schoolYear);
        semesterRepository.save(existingSemester);
    }

    @Override
    public void deleteSemester(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        Semester existingSemester = semesterRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SEMESTER_NOT_FOUND));
        if (!existingSemester.isActive()) {
            throw new AppException(ErrorCode.SEMESTER_IS_DELETED);
        }
        existingSemester.setActive(false);
        semesterRepository.save(existingSemester);
    }

    @Override
    public List<SemesterResponse> searchSemester(SearchSemesterRequest keyword, boolean active) {
        if (keyword == null || keyword.getSemesterName() == null || keyword.getSemesterName().isBlank()) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        String newKeyword = keyword.getSemesterName().trim();
        List<Semester> found;
        if (active) {
            found = semesterRepository.findAllByIsActiveTrueAndSemesterNameContainingIgnoreCase(newKeyword);
        } else {
            found = semesterRepository.findAllByIsActiveFalseAndSemesterNameContainingIgnoreCase(newKeyword);
        }
        if (found.isEmpty()) {
            throw new AppException(ErrorCode.SEMESTER_NOT_FOUND);
        }
        return found.stream()
                .map(semesterMapper::toSemesterResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SemesterResponse> getSemesterBySchoolYear(Long schoolYearId) {
        List<Semester> semester = semesterRepository.findAllBySchoolYear_SchoolYearId(schoolYearId);
        if(semester == null || semester.isEmpty()) {
            throw new AppException(ErrorCode.SEMESTER_NOT_FOUND);
        }

        return semester.stream()
                .map(semesterMapper::toSemesterResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void addTwoSemesterBySchoolYear(Long id, AddSemesterBySchoolYearRequest addSemesterBySchoolYearRequest) {
        List<Semester> semesters = semesterRepository.findAllBySchoolYear_SchoolYearId(id);
        if(semesters.size() > 0)
        {
            throw new AppException(ErrorCode.ENTITY_ALREADY_EXIT,"semester for this school year");
        }
        addSemesterBySchoolYearRequest.getSemester1().setSchoolYearId(id);
        addSemesterBySchoolYearRequest.getSemester2().setSchoolYearId(id);
        createSemester(addSemesterBySchoolYearRequest.getSemester1());
        createSemester(addSemesterBySchoolYearRequest.getSemester2());
    }

    @Override
    public void updateTwoSemesterBySchoolYear(Long id1,Long id2,AddSemesterBySchoolYearRequest addSemesterBySchoolYearRequest) {
        updateSemester(id1, addSemesterBySchoolYearRequest.getSemester1());
        updateSemester(id2, addSemesterBySchoolYearRequest.getSemester2());
    }
}
