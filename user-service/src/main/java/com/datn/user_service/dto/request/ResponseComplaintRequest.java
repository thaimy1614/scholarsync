package com.datn.user_service.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseComplaintRequest {
    private String responseContent;
}
