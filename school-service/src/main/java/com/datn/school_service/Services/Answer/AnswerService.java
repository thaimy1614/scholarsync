package com.datn.school_service.Services.Answer;

import com.datn.school_service.Dto.Request.Answer.AddAnswerRequest;
import com.datn.school_service.Dto.Request.Answer.SearchAnswerRequest;
import com.datn.school_service.Dto.Respone.Answer.AnswerResponse;
import com.datn.school_service.Dto.Respone.Question.InvalidQuestionResponse;
import com.datn.school_service.Exceptions.AppException;
import com.datn.school_service.Exceptions.ErrorCode;
import com.datn.school_service.Mapper.AnswerMapper;
import com.datn.school_service.Models.Answer;
import com.datn.school_service.Models.Question;
import com.datn.school_service.Repository.AnswerRepository;
import com.datn.school_service.Repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnswerService implements AnswerServiceInterface {
    final AnswerRepository answerRepository;

    final QuestionRepository questionRepository;

    final AnswerMapper answerMapper;

    @Override
    public Page<AnswerResponse> getAll(Pageable pageable, boolean active) {
        Page<Answer> answerPage;
        if (active) {
            answerPage = answerRepository.findAllByIsActiveTrue(pageable);
        } else {
            answerPage = answerRepository.findAllByIsActiveFalse(pageable);
        }

        return answerPage.map(answerMapper::toAnswerResponse);

    }

    @Override
    public AnswerResponse getAnswerById(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }

        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ANSWER_NOT_FOUND));

        return answerMapper.toAnswerResponse(answer);
    }

    @Override
    public void createAnswer(AddAnswerRequest addAnswerRequest) {

        if (addAnswerRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        boolean existsAnswer = answerRepository.existsByAnswer(addAnswerRequest.getAnswer());
        if (existsAnswer) {
            throw new AppException(ErrorCode.ANSWER_ALREADY_EXIT);
        }

        Answer answer = answerMapper.toAnswer(addAnswerRequest);

        answerRepository.save(answer);

    }


    @Override
    public void updateAnswer(Long idQues, AddAnswerRequest addAnswerRequest) {
        if (idQues == null || addAnswerRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        Answer existingAnswer = answerRepository.findById(idQues)
                .orElseThrow(() -> new AppException(ErrorCode.ANSWER_NOT_FOUND));

        if (!existingAnswer.getAnswer().equals(addAnswerRequest.getAnswer()) &&
                answerRepository.existsByAnswer(addAnswerRequest.getAnswer())) {
            throw new AppException(ErrorCode.ANSWER_ALREADY_EXIT);
        }
        existingAnswer.setAnswer(addAnswerRequest.getAnswer());
        existingAnswer.setAnswerPoint(addAnswerRequest.getAnswerPoint());
        answerRepository.save(existingAnswer);
    }

    @Override
    public void deleteAnswer(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        Answer existingAnswer = answerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ANSWER_NOT_FOUND));
        if (!existingAnswer.isActive()) {
            throw new AppException(ErrorCode.ANSWER_IS_DELETED);
        }
        existingAnswer.setActive(false);
        answerRepository.save(existingAnswer);
    }

    @Override
    public void restoreAnswer(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        Answer existingAnswer = answerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ANSWER_NOT_FOUND));
        if (existingAnswer.isActive()) {
            throw new AppException(ErrorCode.ENTITY_IS_ACTIVE, existingAnswer.getAnswer());
        }
        existingAnswer.setActive(true);
        answerRepository.save(existingAnswer);
    }

    @Override
    public List<AnswerResponse> searchAnswer(SearchAnswerRequest keyword, boolean active) {
        if (keyword == null || keyword.getAnswer() == null || keyword.getAnswer().isBlank()) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        String newKeyword = keyword.getAnswer().trim();
        List<Answer> found;
        if (active) {
            found = answerRepository.findAllByIsActiveTrueAndAnswerContainingIgnoreCase(newKeyword);
        } else {
            found = answerRepository.findAllByIsActiveFalseAndAnswerContainingIgnoreCase(newKeyword);
        }
        if (found.isEmpty()) {
            throw new AppException(ErrorCode.ANSWER_NOT_FOUND);
        }
        return found.stream()
                .map(answerMapper::toAnswerResponse)
                .collect(Collectors.toList());
    }

}
