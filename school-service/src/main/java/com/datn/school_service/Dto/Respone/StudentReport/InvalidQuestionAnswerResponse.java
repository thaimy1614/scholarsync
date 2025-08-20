package com.datn.school_service.Dto.Respone.StudentReport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvalidQuestionAnswerResponse {
    private Long questionId;
    private Long answerId;
}
