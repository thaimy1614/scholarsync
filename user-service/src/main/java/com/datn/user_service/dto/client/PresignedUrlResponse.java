package com.datn.user_service.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PresignedUrlResponse {
    private int rowIndex;
    private String presignedUrl;
    private String imageUrl;
}
