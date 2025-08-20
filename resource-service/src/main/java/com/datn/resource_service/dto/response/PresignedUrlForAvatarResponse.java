package com.datn.resource_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PresignedUrlForAvatarResponse {
    private int rowIndex;
    private String presignedUrl;
    private String imageUrl;
}
