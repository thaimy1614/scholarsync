package com.datn.school_service.Dto.Request.NewsType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNewTypeRequest {
    private String newsTypeDescription;

    private String newsTypeName;

    private boolean isActive;
}
