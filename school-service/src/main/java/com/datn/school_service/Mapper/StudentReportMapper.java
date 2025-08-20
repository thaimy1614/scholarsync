package com.datn.school_service.Mapper;

import com.datn.school_service.Dto.Respone.StudentReport.QuestionAnswerResponse;
import com.datn.school_service.Dto.Respone.StudentReport.TotalPointOneStudentResponse;
import com.datn.school_service.Dto.Respone.User.GetUserNameResponse;
import com.datn.school_service.Models.Answer;
import com.datn.school_service.Models.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentReportMapper {
    @Mapping(target = "questionId", source = "question.questionId")
    @Mapping(target = "question", source = "question.question")
    @Mapping(target = "answerId", source = "answer.answerId")
    @Mapping(target = "answer", source = "answer.answer")
    @Mapping(target = "averagePoint", source="averagePoint")
   // @Mapping(target = "answerPoint", source = "answer.answerPoint")
    QuestionAnswerResponse toQuestionAnswerResponse(Question question, Answer answer,Double averagePoint);

    @Mapping(target = "studentId", source = "studentInfo.userId")
    @Mapping(target = "studentName", source = "studentInfo.fullName")
    @Mapping(target = "image", source = "studentInfo.image")
    @Mapping(target = "total_point", source = "totalPoint")
    TotalPointOneStudentResponse toTotalPointOneStudentResponse(GetUserNameResponse studentInfo, int totalPoint);
}
