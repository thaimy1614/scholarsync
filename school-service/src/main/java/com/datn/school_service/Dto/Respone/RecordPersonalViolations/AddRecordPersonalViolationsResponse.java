package com.datn.school_service.Dto.Respone.RecordPersonalViolations;

import com.datn.school_service.Dto.Respone.User.GetUserNameResponse;
import com.datn.school_service.Dto.Respone.ViolationType.ViolationTypeResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddRecordPersonalViolationsResponse {
    private GetUserNameResponse studentInfo;
    private List<ViolationTypeResponse> listViolationType;
}
