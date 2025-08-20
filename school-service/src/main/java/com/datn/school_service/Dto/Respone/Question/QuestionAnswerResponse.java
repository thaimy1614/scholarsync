package com.datn.school_service.Dto.Respone.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class QuestionAnswerResponse {
    private Long questionId;
    private String question;
    private String feedBack;
}
