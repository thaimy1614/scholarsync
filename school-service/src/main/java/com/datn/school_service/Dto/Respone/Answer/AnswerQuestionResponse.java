package com.datn.school_service.Dto.Respone.Answer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class AnswerQuestionResponse {

    private Long answerId;
    private String answer;

    private int answerPoint;
}
