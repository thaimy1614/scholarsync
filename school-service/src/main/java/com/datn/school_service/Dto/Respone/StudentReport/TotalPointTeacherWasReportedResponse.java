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
public class TotalPointTeacherWasReportedResponse {
    private int totalPointTeacherWasReported;
    private String image;
    private String teacherId;
    private String teacherName;
    private Long semesterId;
    private String semester;
    private Long schoolYearId;
    private String schoolYear;
    private List<TotalPointOneStudentResponse> totalPointOneStudentResponses;

}
