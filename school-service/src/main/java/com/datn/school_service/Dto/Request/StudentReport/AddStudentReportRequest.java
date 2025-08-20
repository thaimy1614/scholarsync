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
public class AddStudentReportRequest {

    private Long classId;
    private String studentId;
    private String teacherId;
    private Long semesterId;
    private Long subjectId;
   // private Long schoolYearId;
    private List<QuestionAnswerRequest> questionAnswer;

}
