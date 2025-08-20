package com.datn.attendance_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClassResponse {
    private String classId;
    private String className;
}
