package com.datn.school_service.Dto.Request.ViolationRecord;

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
public class AddViolationRecordRequest {
  //  private String violationStudentId;
    private int absentCount;
    private Long classId;
    private String redFlagId;
    private Long violationTypeId;
}

