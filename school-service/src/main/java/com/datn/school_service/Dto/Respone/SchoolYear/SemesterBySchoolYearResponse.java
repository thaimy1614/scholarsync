package com.datn.school_service.Dto.Respone.SchoolYear;

import com.datn.school_service.Dto.Respone.Semester.SemesterResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SemesterBySchoolYearResponse {
    private Long schoolYearId;

    private String schoolYear;

    private List<SemesterResponse> semester;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;
}
