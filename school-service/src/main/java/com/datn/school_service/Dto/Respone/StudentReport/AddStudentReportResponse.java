package com.datn.school_service.Dto.Respone.StudentReport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddStudentReportResponse {
    private Long evaluationSessionId;
    private Long schoolYearId;
    private String schoolYear;
    private String studentId;
    private String studentName;
    private String teacherId;
    private String teacherName;
    private Long semesterId;
    private String semesterName;
    private Long classId;
    private String className;
    private String subjectName;
    private double averagePoint;
    private List<QuestionAnswerResponse> questionAnswerResponses;
    private List<InvalidQuestionAnswerResponse> invalidQuestionAnswer;

}
