package com.datn.school_service.Dto.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddNewsTypeRequest {

    private String newsTypeDescription;

    private String newsTypeName;
}
