package com.datn.school_service.Dto.Request.StudentReport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TotalPointTeacherWasReportedRequest {
    private List<String> studentId;
    private String teacherId;
    private Long semesterId;
    private Long classId;


}
