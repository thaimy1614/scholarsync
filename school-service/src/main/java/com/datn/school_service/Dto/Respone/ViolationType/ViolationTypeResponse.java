package com.datn.school_service.Dto.Respone.ViolationType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
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
public class ViolationTypeResponse {
    private Long violationTypeId;
    private String violationTypeName;
    private int violationPoint;
    private String violationCategory;

}
