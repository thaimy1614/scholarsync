package com.datn.school_service.Mapper;

import com.datn.school_service.Dto.Request.Answer.AddAnswerRequest;
import com.datn.school_service.Dto.Respone.Answer.AnswerResponse;
import com.datn.school_service.Dto.Respone.Question.QuestionAnswerResponse;
import com.datn.school_service.Models.Answer;
import com.datn.school_service.Models.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AnswerMapper {

     Answer toAnswer(AddAnswerRequest addAnswerRequest);

     AnswerResponse toAnswerResponse(Answer answer);

}