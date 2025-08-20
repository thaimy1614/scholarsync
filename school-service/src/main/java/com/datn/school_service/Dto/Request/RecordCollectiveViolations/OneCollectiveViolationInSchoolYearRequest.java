package com.datn.school_service.Dto.Request.RecordCollectiveViolations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OneCollectiveViolationInSchoolYearRequest {
    private String className;
    private String schoolYear;

}
