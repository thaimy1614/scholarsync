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
public class QuestionAnswerResponse {
    private Long studentReportDetailId;
   // private Long evaluationSessionId;
    private Long questionId;
    private String question;
    private Long answerId;
    private String answer;
    private double averagePoint;
}
