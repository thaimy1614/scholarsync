package com.datn.school_service.Dto.Respone.ViolationRecord;
import com.datn.school_service.Dto.Respone.User.GetUserNameResponse;
import com.datn.school_service.Dto.Respone.ViolationType.ViolationTypeResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ViolationRecordResponse {
    private Long ViolationRecordId;
  //  private GetUserNameResponse violationStudent;
    private int absentCount;
    private String className;
    private GetUserNameResponse redFlag;
    private ViolationTypeResponse violationTypeResponse;
}
