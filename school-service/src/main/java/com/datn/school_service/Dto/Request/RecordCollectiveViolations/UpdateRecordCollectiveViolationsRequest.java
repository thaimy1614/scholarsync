package com.datn.school_service.Dto.Request.RecordCollectiveViolations;

import com.datn.school_service.Dto.Request.RecordPersonalViolations.AddRecordPersonalViolationsRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateRecordCollectiveViolationsRequest {
    private Long classId;
    private int absentCount;
    private String redFlagId;
    private String principalId;
    private List<Long> violationGroupId; // co the co vi pham hoac ko
    List<AddRecordPersonalViolationsRequest> listStudentViolation;
}
