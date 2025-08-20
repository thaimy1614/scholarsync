package com.datn.school_service.Dto.Respone;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BasicClassResponse {
    private Long classId;
    private String className;
}
