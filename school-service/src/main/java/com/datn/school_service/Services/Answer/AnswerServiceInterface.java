package com.datn.school_service.Services.Answer;

import com.datn.school_service.Dto.Request.Answer.AddAnswerRequest;
import com.datn.school_service.Dto.Request.Answer.SearchAnswerRequest;
import com.datn.school_service.Dto.Respone.Answer.AnswerResponse;
import com.datn.school_service.Dto.Respone.Question.InvalidQuestionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AnswerServiceInterface {
    Page<AnswerResponse> getAll(Pageable pageable, boolean active);

    AnswerResponse getAnswerById(Long id);

    void createAnswer(AddAnswerRequest addAnswerRequest);

    void updateAnswer(Long id, AddAnswerRequest addAnswerRequest );

    void deleteAnswer(Long id);

    void restoreAnswer(Long id);

    List<AnswerResponse> searchAnswer(SearchAnswerRequest keyword, boolean active);
}
