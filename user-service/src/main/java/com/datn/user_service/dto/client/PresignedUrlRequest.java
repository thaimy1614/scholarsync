package com.datn.user_service.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PresignedUrlRequest {
    private int rowIndex;
    private String fileName;
    private String contentType;
}

