package com.datn.school_service.Mapper;

import com.datn.school_service.Dto.Respone.StudentReport.AddStudentReportResponse;
import com.datn.school_service.Dto.Respone.StudentReport.QuestionAnswerResponse;
import com.datn.school_service.Dto.Respone.StudentReport.TotalPointTeacherWasReportedResponse;
import com.datn.school_service.Models.EvaluationSession;
import com.datn.school_service.Models.StudentReportDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EvaluationSessionMapper {
    @Mapping(source = "evaluationSessionId", target = "evaluationSessionId")
    @Mapping(source = "studentId", target = "studentId")
    @Mapping(source = "teacherId", target = "teacherId")
    @Mapping(source = "clazz.classId", target = "classId")
    @Mapping(source = "clazz.className", target = "className")
    @Mapping(source = "semester.semesterId", target = "semesterId")
    @Mapping(source = "semester.semesterName", target = "semesterName")
    @Mapping(source = "averageReportPoint", target = "averagePoint")
    AddStudentReportResponse toDto(EvaluationSession session);

    @Mapping(source = "studentReportDetailId", target = "studentReportDetailId")
    @Mapping(source = "question.questionId", target = "questionId")
    @Mapping(source = "question.question", target = "question")
    @Mapping(source = "answer.answerId", target = "answerId")
    @Mapping(source = "answer.answer", target = "answer")
    QuestionAnswerResponse toDto(StudentReportDetail detail);

    List<QuestionAnswerResponse> toDtoList(List<StudentReportDetail> details);
}
