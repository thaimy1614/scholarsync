package com.datn.school_service.Dto.Respone.Teacherlassification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherlassificationResponse {

    private Long teacherClassificationId;

    private String teacherId;

    private String teacherName;

    private String image;

    private String teacherClassificationName;

    private double teacherClassificationPoint;

    private SemesterResponse semesterResponse;

    private ClassResponseTeacherlassification classResponse;
}
