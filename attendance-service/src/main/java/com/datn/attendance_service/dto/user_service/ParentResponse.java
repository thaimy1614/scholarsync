package com.datn.attendance_service.dto.user_service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParentResponse {
    private String id;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private String avatar;
    private Boolean isNotificationOn;
}
