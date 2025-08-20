package com.datn.school_service.Dto.Respone.Answer;


import com.datn.school_service.Dto.Respone.Question.QuestionAnswerResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class AnswerResponse {
    private Long answerId;
    private String answer;

    private int answerPoint;

   //private List<QuestionAnswerResponse> listQuestionResponse;
}
