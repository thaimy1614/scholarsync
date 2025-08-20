package com.datn.school_service.Services.Question;

import com.datn.school_service.Dto.Request.Question.AddQuestionRequest;
import com.datn.school_service.Dto.Request.Question.SearchQuestionRequest;
import com.datn.school_service.Dto.Respone.Answer.InvalidAnswerResponse;
import com.datn.school_service.Dto.Respone.Question.QuestionResponse;
import com.datn.school_service.Exceptions.AppException;
import com.datn.school_service.Exceptions.ErrorCode;
import com.datn.school_service.Mapper.QuestionMapper;
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
public class QuestionService implements QuestionServiceInterface {

    final QuestionRepository questionRepository;
    final AnswerRepository answerRepository;
    final QuestionMapper questionMapper;

    @Override
    public Page<QuestionResponse> getAll(Pageable pageable, boolean active) {
        Page<Question> questionPage;
        if (active) {
            questionPage = questionRepository.findAllByIsActiveTrue(pageable);
        } else {
            questionPage = questionRepository.findAllByIsActiveFalse(pageable);
        }

        return questionPage.map(questionMapper::toQuestionResponse);

    }

    @Override
    public QuestionResponse getQuestionById(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }

        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        return questionMapper.toQuestionResponse(question);
    }

    @Override
    public InvalidAnswerResponse createQuestion(AddQuestionRequest addQuestionRequest) {
        InvalidAnswerResponse invalidAnswerResponse = new InvalidAnswerResponse();
        List<Long> invalidAnswer = new ArrayList<>();

        if (addQuestionRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }

        boolean existsQuestion = questionRepository.existsByQuestion(addQuestionRequest.getQuestion());
        if (existsQuestion) {
            throw new AppException(ErrorCode.QUESTION_ALREADY_EXIT);
        }
        Set<Answer> answers = new HashSet<>();
        for (Long id : addQuestionRequest.getAnswerIds()) {
            try
            {
                Answer answer = answerRepository.findByAnswerIdAndIsActiveTrue(id)
                        .orElseThrow(() -> new AppException(ErrorCode.ANSWER_NOT_FOUND));
                answers.add(answer);
            }
            catch (AppException e)
            {
                invalidAnswer.add(id);
            }

        }
        invalidAnswerResponse.setInvalidAnswerId(invalidAnswer);
        Question question = questionMapper.toQuestion(addQuestionRequest);
        question.setAnswers(answers);
        questionRepository.save(question);
        return invalidAnswerResponse;
    }


    @Override
    public InvalidAnswerResponse updateQuestion(Long idQues, AddQuestionRequest addQuestionRequest) {
        InvalidAnswerResponse invalidAnswerResponse = new InvalidAnswerResponse();
        List<Long> invalidAnswer = new ArrayList<>();
        if (idQues == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }

        Question existingQuestion = questionRepository.findById(idQues)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        if (addQuestionRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        if (!existingQuestion.getQuestion().equals(addQuestionRequest.getQuestion()) &&
                questionRepository.existsByQuestion(addQuestionRequest.getQuestion())) {
            throw new AppException(ErrorCode.QUESTION_ALREADY_EXIT);
        }
        Set<Answer> answers = new HashSet<>();
        for (Long id : addQuestionRequest.getAnswerIds()) {
            try
            {
                Answer answer = answerRepository.findByAnswerIdAndIsActiveTrue(id)
                        .orElseThrow(() -> new AppException(ErrorCode.ANSWER_NOT_FOUND));
                answers.add(answer);
            }
            catch (AppException e)
            {
                invalidAnswer.add(id);
            }

        }
        invalidAnswerResponse.setInvalidAnswerId(invalidAnswer);
        existingQuestion.setAnswers(new HashSet<>(answers));
        existingQuestion.setQuestion(addQuestionRequest.getQuestion());
        questionRepository.save(existingQuestion);
        return invalidAnswerResponse;
    }

    @Override
    public void deleteQuestion(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        Question existingQuestion = questionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
        if(!existingQuestion.isActive())
        {
            throw new AppException(ErrorCode.QUESTION_IS_DELETED);
        }
        existingQuestion.setActive(false);
        questionRepository.save(existingQuestion);
    }

    @Override
    public void restoreQuestion(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        Question existingQuestion = questionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
        if(existingQuestion.isActive())
        {
            throw new AppException(ErrorCode.ENTITY_IS_ACTIVE,existingQuestion.getQuestion());
        }
        existingQuestion.setActive(false);
        questionRepository.save(existingQuestion);
    }

    @Override
    public List<QuestionResponse> searchQuestion(SearchQuestionRequest keyword, boolean active) {
        if (keyword == null || keyword.getQuestion() == null || keyword.getQuestion().isBlank()) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        String newKeyword = keyword.getQuestion().trim();
        List<Question> found;
        if (active) {
            found = questionRepository.findAllByIsActiveTrueAndQuestionContainingIgnoreCase(newKeyword);
        } else {
            found = questionRepository.findAllByIsActiveFalseAndQuestionContainingIgnoreCase(newKeyword);
        }
        if (found.isEmpty()) {
            throw new AppException(ErrorCode.QUESTION_NOT_FOUND);
        }
        return found.stream()
                .map(questionMapper::toQuestionResponse)
                .collect(Collectors.toList());
    }

}
