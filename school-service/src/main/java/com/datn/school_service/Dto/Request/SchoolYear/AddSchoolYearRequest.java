package com.datn.school_service.Dto.Request.SchoolYear;
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
public class AddSchoolYearRequest {

    private String schoolYear;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;
}

