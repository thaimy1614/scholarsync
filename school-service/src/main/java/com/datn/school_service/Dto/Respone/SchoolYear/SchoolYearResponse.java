package com.datn.school_service.Dto.Respone.SchoolYear;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchoolYearResponse {
    private Long schoolYearId;

    private String schoolYear;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;



}
