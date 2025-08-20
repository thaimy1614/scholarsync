package com.datn.school_service.Dto.Respone.Semester;
import com.datn.school_service.Dto.Respone.SchoolYear.SchoolYearResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterResponse {
    private Long semesterId;
    private String semesterName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private SchoolYearResponse schoolYearResponse;

}
