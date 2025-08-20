package com.datn.school_service.Dto.Request.Semester;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddSemesterBySchoolYearRequest {
    AddSemesterRequest semester1;
    AddSemesterRequest semester2;
}
