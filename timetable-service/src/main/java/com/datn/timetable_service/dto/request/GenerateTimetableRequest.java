package com.datn.timetable_service.dto.request;

import lombok.Data;

@Data
public class GenerateTimetableRequest {
    private Long semesterId;
    private Long schoolYearId;
    private Long weekNumber;
}
