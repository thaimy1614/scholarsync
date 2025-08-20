package com.datn.resource_service.dto.gemini;

import lombok.Data;
import java.util.List;

@Data
public class QuestionAnswerSet {
    private String summary;
    private List<Question> questions;

    @Data
    public static class Question {
        private String question;
        private List<String> options;
        private String answer;
    }
}
