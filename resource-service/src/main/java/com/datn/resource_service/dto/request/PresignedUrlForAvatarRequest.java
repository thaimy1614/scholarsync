package com.datn.resource_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PresignedUrlForAvatarRequest {
    private int rowIndex;
    private String fileName;
    private String contentType;
}
