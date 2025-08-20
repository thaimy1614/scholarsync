package com.datn.school_service.Dto.Respone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddStudentResponse {
    private String className;
    private List<String> invalidStudentIds;

}
