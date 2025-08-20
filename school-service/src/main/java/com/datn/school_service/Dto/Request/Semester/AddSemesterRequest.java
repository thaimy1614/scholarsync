package com.datn.school_service.Dto.Request.Semester;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddSemesterRequest {
    private String semesterName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long schoolYearId;
    private String description;
}
