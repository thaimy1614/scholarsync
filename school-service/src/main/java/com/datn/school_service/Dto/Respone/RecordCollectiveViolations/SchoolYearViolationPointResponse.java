package com.datn.school_service.Dto.Respone.RecordCollectiveViolations;

import java.util.List;

public class SchoolYearViolationPointResponse {
    private String schoolYear;
    private String className;
    List<WeeklyViolationPointResponse> weeklyViolationPointResponseList;
}
