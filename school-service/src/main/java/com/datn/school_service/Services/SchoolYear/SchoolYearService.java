package com.datn.school_service.Services.SchoolYear;

import com.datn.school_service.Dto.Request.SchoolYear.AddSchoolYearRequest;
import com.datn.school_service.Dto.Request.SchoolYear.SearchSchoolYearRequest;
import com.datn.school_service.Dto.Respone.SchoolYear.GetDayOfWeekResponse;
import com.datn.school_service.Dto.Respone.SchoolYear.SchoolYearResponse;
import com.datn.school_service.Dto.Respone.SchoolYear.SemesterBySchoolYearResponse;
import com.datn.school_service.Dto.Respone.Semester.SemesterResponse;
import com.datn.school_service.Exceptions.AppException;
import com.datn.school_service.Exceptions.ErrorCode;
import com.datn.school_service.Mapper.SchoolYearMapper;
import com.datn.school_service.Mapper.SemesterMapper;
import com.datn.school_service.Models.SchoolYear;
import com.datn.school_service.Models.Semester;
import com.datn.school_service.Repository.SchoolYearRepository;
import com.datn.school_service.Repository.QuestionRepository;
import com.datn.school_service.Repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolYearService implements SchoolYearServiceInterface {
    final SchoolYearRepository schoolYearRepository;

    final SchoolYearMapper schoolYearMapper;

    final SemesterRepository semesterRepository;

    final SemesterMapper semesterMapper;

    @Override
    public Page<SchoolYearResponse> getAll(Pageable pageable, boolean active) {
        Page<SchoolYear> schoolYearPage;
        if (active) {
            schoolYearPage = schoolYearRepository.findAllByIsActiveTrue(pageable);
        } else {
            schoolYearPage = schoolYearRepository.findAllByIsActiveFalse(pageable);
        }

        return schoolYearPage.map(schoolYearMapper::toSchoolYearResponse);

    }

    @Override
    public SchoolYearResponse getSchoolYearById(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }

        SchoolYear schoolYear = schoolYearRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SCHOOL_YEAR_NOT_FOUND));

        return schoolYearMapper.toSchoolYearResponse(schoolYear);
    }

    @Override
    public void createSchoolYear(AddSchoolYearRequest addSchoolYearRequest) {

        if(addSchoolYearRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        boolean exists1 = schoolYearRepository.existsByStartDate(addSchoolYearRequest.getStartDate());
        boolean exists2 = schoolYearRepository.existsByEndDate(addSchoolYearRequest.getEndDate());

        if (exists1 || exists2) {

            throw new AppException(ErrorCode.SCHOOL_YEAR_START_END_DATE_EXIT);
        }


        boolean existsSchoolYear = schoolYearRepository.existsBySchoolYear(addSchoolYearRequest.getSchoolYear());
        if (existsSchoolYear) {
            throw new AppException(ErrorCode.SCHOOL_YEAR_ALREADY_EXIT);
        }
        if (addSchoolYearRequest.getStartDate().isAfter(addSchoolYearRequest.getEndDate()) ||
                addSchoolYearRequest.getStartDate().isEqual(addSchoolYearRequest.getEndDate())) {
            throw new AppException(ErrorCode.SCHOOL_YEAR_INVALID_DATE);
        }

        SchoolYear schoolYear = schoolYearMapper.toSchoolYear(addSchoolYearRequest);

        schoolYearRepository.save(schoolYear);

    }


    @Override
    public void updateSchoolYear(Long idQues, AddSchoolYearRequest addSchoolYearRequest) {
        if (idQues == null || addSchoolYearRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }

        SchoolYear existingSchoolYear = schoolYearRepository.findById(idQues)
                .orElseThrow(() -> new AppException(ErrorCode.SCHOOL_YEAR_NOT_FOUND));


        if (!existingSchoolYear.getSchoolYear().equals(addSchoolYearRequest.getSchoolYear()) &&
                schoolYearRepository.existsBySchoolYear(addSchoolYearRequest.getSchoolYear())) {
            throw new AppException(ErrorCode.SCHOOL_YEAR_ALREADY_EXIT);
        }


        if (addSchoolYearRequest.getStartDate().isAfter(addSchoolYearRequest.getEndDate()) ||
                addSchoolYearRequest.getStartDate().isEqual(addSchoolYearRequest.getEndDate())) {
            throw new AppException(ErrorCode.SCHOOL_YEAR_INVALID_DATE);
        }


        if (!existingSchoolYear.getStartDate().equals(addSchoolYearRequest.getStartDate()) &&
                schoolYearRepository.existsByStartDate(addSchoolYearRequest.getStartDate())) {
            throw new AppException(ErrorCode.SCHOOL_YEAR_START_END_DATE_EXIT);
        }


        if (!existingSchoolYear.getEndDate().equals(addSchoolYearRequest.getEndDate()) &&
                schoolYearRepository.existsByEndDate(addSchoolYearRequest.getEndDate())) {
            throw new AppException(ErrorCode.SCHOOL_YEAR_START_END_DATE_EXIT);
        }


        schoolYearMapper.updateSchoolYear(existingSchoolYear, addSchoolYearRequest);
        schoolYearRepository.save(existingSchoolYear);
    }


    @Override
    public void deleteSchoolYear(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        SchoolYear existingSchoolYear = schoolYearRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SCHOOL_YEAR_NOT_FOUND));
        if (!existingSchoolYear.isActive()) {
            throw new AppException(ErrorCode.SCHOOL_YEAR_IS_DELETED);
        }
        existingSchoolYear.setActive(false);
        schoolYearRepository.save(existingSchoolYear);
    }

    @Override
    public List<SchoolYearResponse> searchSchoolYear(SearchSchoolYearRequest keyword, boolean active) {
        if (keyword == null || keyword.getSchoolYear() == null || keyword.getSchoolYear().isBlank()) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        String newKeyword = keyword.getSchoolYear().trim();
        List<SchoolYear> found;
        if (active) {
            found = schoolYearRepository.findAllByIsActiveTrueAndSchoolYearContainingIgnoreCase(newKeyword);
        } else {
            found = schoolYearRepository.findAllByIsActiveFalseAndSchoolYearContainingIgnoreCase(newKeyword);
        }
        if (found.isEmpty()) {
            throw new AppException(ErrorCode.SCHOOL_YEAR_NOT_FOUND);
        }
        return found.stream()
                .map(schoolYearMapper::toSchoolYearResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SemesterBySchoolYearResponse getAllSemesterBySchoolYearId(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.INPUT_NULL, "schoolyear id");
        }

        SchoolYear schoolYear = schoolYearRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SCHOOL_YEAR_NOT_FOUND));

        List<Semester> semesters = semesterRepository.findAllBySchoolYear_SchoolYearId(id);

        if (semesters == null || semesters.isEmpty()) {
            return null;
        }

        List<SemesterResponse> sortedSemesters = semesters.stream()
                .sorted(Comparator.comparingInt(s -> extractNumberFromSemesterName(s.getSemesterName())))
                .map(semesterMapper::toSemesterResponse)
                .collect(Collectors.toList());

        SemesterBySchoolYearResponse response = SemesterBySchoolYearResponse.builder()
                .schoolYearId(schoolYear.getSchoolYearId())
                .schoolYear(schoolYear.getSchoolYear())
                .startDate(schoolYear.getStartDate())
                .endDate(schoolYear.getEndDate())
                .description(schoolYear.getDescription())
                .semester(sortedSemesters)
                .build();

        return response;
    }

    @Override
    public Page<SemesterBySchoolYearResponse> getAllSchoolYearWithSemesterById(Pageable pageable, boolean active) {
        Page<SchoolYear> schoolYearPage;
       if(active) {
           schoolYearPage =    schoolYearRepository.findAllByIsActiveTrue(pageable);
       }
       else
       {
           schoolYearPage =  schoolYearRepository.findAllByIsActiveFalse(pageable);
       }
        return schoolYearPage.map(schoolYear -> {
            List<Semester> semesters = semesterRepository.findAllBySchoolYear_SchoolYearId(schoolYear.getSchoolYearId());
            List<SemesterResponse> sortedSemesters = semesters.stream()
                    .sorted(Comparator.comparingInt(s -> extractNumberFromSemesterName(s.getSemesterName())))
                    .map(semesterMapper::toSemesterResponse)
                    .collect(Collectors.toList());

            return SemesterBySchoolYearResponse.builder()
                    .schoolYearId(schoolYear.getSchoolYearId())
                    .schoolYear(schoolYear.getSchoolYear())
                    .startDate(schoolYear.getStartDate())
                    .endDate(schoolYear.getEndDate())
                    .description(schoolYear.getDescription())
                    .semester(sortedSemesters)
                    .build();
        });
    }


    private int extractNumberFromSemesterName(String name) {
        if (name == null) return Integer.MAX_VALUE;
        Matcher matcher = Pattern.compile("\\d+").matcher(name);
        return matcher.find() ? Integer.parseInt(matcher.group()) : Integer.MAX_VALUE;
    }

    @Override
    public List<SchoolYearResponse> getSchoolYearByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        List<SchoolYear> schoolYears = schoolYearRepository.findAllById(ids);
        if (schoolYears.isEmpty()) {
            throw new AppException(ErrorCode.SCHOOL_YEAR_NOT_FOUND);
        }
        return schoolYears.stream()
                .map(schoolYearMapper::toSchoolYearResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GetDayOfWeekResponse> getAllWeeksBySchoolYear(Long schoolYearId) {
        SchoolYear schoolYear = schoolYearRepository.findById(schoolYearId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHOOL_YEAR_NOT_FOUND));

        LocalDate start = schoolYear.getStartDate();
        LocalDate end = schoolYear.getEndDate();

        List<GetDayOfWeekResponse> result = new ArrayList<>();
        int week = 1;

        LocalDate current = start;
        while (!current.isAfter(end)) {
            LocalDate weekStart = current;
            LocalDate weekEnd = current.plusDays(6);
            if (weekEnd.isAfter(end)) {
                weekEnd = end;
            }

            result.add(new GetDayOfWeekResponse(week++, weekStart, weekEnd));
            current = weekEnd.plusDays(1);
        }

        return result;
    }


}
