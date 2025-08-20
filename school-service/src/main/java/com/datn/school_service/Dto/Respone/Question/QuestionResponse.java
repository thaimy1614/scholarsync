package com.datn.school_service.Dto.Respone.Question;


import com.datn.school_service.Dto.Respone.Answer.AnswerQuestionResponse;
import com.datn.school_service.Dto.Respone.Answer.AnswerResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {
    private Long questionId;

    private String question;

    private List<AnswerQuestionResponse> listAnswerResponse;

}
