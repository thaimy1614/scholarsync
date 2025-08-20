package com.datn.school_service.Dto.Respone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsTypeResponse {
    private Long newsTypeId;

    private String newsTypeDescription;

    private String newsTypeName;
}
