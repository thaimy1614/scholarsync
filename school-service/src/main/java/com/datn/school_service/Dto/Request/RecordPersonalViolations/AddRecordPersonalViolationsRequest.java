package com.datn.school_service.Dto.Request.RecordPersonalViolations;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddRecordPersonalViolationsRequest {
    private String studentId;
    private List<Long> violationTypeId;
}
