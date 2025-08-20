package com.datn.attendance_service.dto.user_service;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StudentResponse {
    private String userId;
    private String fullName;
    private List<ParentResponse> parents;
}
