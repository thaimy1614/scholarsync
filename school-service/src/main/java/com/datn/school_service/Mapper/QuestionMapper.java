package com.datn.school_service.Mapper;

import com.datn.school_service.Dto.Request.Question.AddQuestionRequest;
import com.datn.school_service.Dto.Respone.Answer.AnswerQuestionResponse;
import com.datn.school_service.Dto.Respone.Question.QuestionResponse;
import com.datn.school_service.Models.Answer;
import com.datn.school_service.Models.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    Question toQuestion(AddQuestionRequest addQuestionRequest);

    @Mapping(target = "listAnswerResponse", source = "answers", qualifiedByName = "mapAnswers")
    QuestionResponse toQuestionResponse(Question question);

    void toUpdateQuestion(@MappingTarget Question question, AddQuestionRequest addQuestionRequest);

    @Named("mapAnswers")
    default List<AnswerQuestionResponse> mapAnswers(Set<Answer> answers) {
        return answers.stream()
                .map(this::toAnswerQuestionResponse)
                .collect(Collectors.toList());
    }

    default AnswerQuestionResponse toAnswerQuestionResponse(Answer answer) {
        return AnswerQuestionResponse.builder()
                .answerId(answer.getAnswerId())
                .answer(answer.getAnswer())
                .answerPoint(answer.getAnswerPoint())
                .build();
    }

}
