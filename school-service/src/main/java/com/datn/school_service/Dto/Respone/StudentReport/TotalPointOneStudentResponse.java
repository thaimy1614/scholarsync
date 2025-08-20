package com.datn.school_service.Dto.Respone.StudentReport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TotalPointOneStudentResponse {
    private int total_point;
    private String studentName;
    private String studentId;
    private String image;
}
