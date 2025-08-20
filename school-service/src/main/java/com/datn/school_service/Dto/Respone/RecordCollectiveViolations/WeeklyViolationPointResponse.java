package com.datn.school_service.Dto.Respone.RecordCollectiveViolations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeeklyViolationPointResponse {
    private int week;
    private long totalViolationPoint;
    public long getClassPoint() {
        return 100 - totalViolationPoint;
    }
    private String className;

}