package com.datn.school_service.Dto.Request.Teacherlassification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherClassificationInSemesterRequest {
    private String teacherId;
    private Long semesterId;
}
