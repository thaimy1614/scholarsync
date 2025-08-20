package com.datn.school_service.Dto.Request.Answer;

import lombok.Builder;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddAnswerRequest {
    private String answer;

    private int answerPoint;

}
