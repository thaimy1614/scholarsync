package com.datn.school_service.Dto.Request.Class;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddClassRequest {
    private String className;

//    private String teacherId;

    private Long schoolYearId;

    private String MainSession;

    private Long roomId;

    private Long gradeId;
}
