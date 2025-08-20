package com.datn.school_service.Services.Question;


import com.datn.school_service.Dto.Request.Question.AddQuestionRequest;
import com.datn.school_service.Dto.Request.Question.SearchQuestionRequest;
import com.datn.school_service.Dto.Respone.Answer.InvalidAnswerResponse;
import com.datn.school_service.Dto.Respone.Question.QuestionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuestionServiceInterface {
    Page<QuestionResponse> getAll(Pageable pageable, boolean active);

    QuestionResponse getQuestionById(Long id);

    InvalidAnswerResponse createQuestion(AddQuestionRequest addQuestionRequest);

    InvalidAnswerResponse updateQuestion(Long id, AddQuestionRequest addQuestionRequest );

    void deleteQuestion(Long id);

    void restoreQuestion(Long id);

    List<QuestionResponse> searchQuestion(SearchQuestionRequest keyword, boolean active);
}
