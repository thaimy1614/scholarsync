package com.datn.timetable_service.service;

import com.datn.timetable_service.client.SchoolServiceClient;
import com.datn.timetable_service.dto.SchoolService.SchoolYearResponse;
import com.datn.timetable_service.dto.SchoolService.SemesterResponse;
import com.datn.timetable_service.dto.request.ExamSessionRequest;
import com.datn.timetable_service.dto.response.ExamSessionResponse;
import com.datn.timetable_service.exception.AppException;
import com.datn.timetable_service.exception.ErrorCode;
import com.datn.timetable_service.model.ExamSession;
import com.datn.timetable_service.repository.ExamSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamSessionService {
    private final ExamSessionRepository examSessionRepository;
    private final SchoolServiceClient schoolServiceClient;

    public ExamSessionResponse createExamSession(ExamSessionRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new AppException(ErrorCode.EXAM_SESSION_CONFLICT);
        }
        ExamSession session = ExamSession.builder()
                .semesterId(request.getSemesterId())
                .schoolYearId(request.getSchoolYearId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        session = examSessionRepository.save(session);
        return mapToResponse(session);
    }

    public ExamSessionResponse updateExamSession(Long examSessionId, ExamSessionRequest request) {
        ExamSession session = examSessionRepository.findById(examSessionId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_SESSION_NOT_FOUND));
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new AppException(ErrorCode.EXAM_SESSION_CONFLICT);
        }
        session.setSemesterId(request.getSemesterId());
        session.setSchoolYearId(request.getSchoolYearId());
        session.setStartDate(request.getStartDate());
        session.setEndDate(request.getEndDate());
        session = examSessionRepository.save(session);
        return mapToResponse(session);
    }

    public void deleteExamSession(Long examSessionId) {
        ExamSession session = examSessionRepository.findById(examSessionId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_SESSION_NOT_FOUND));
        examSessionRepository.delete(session);
    }

    public ExamSessionResponse getExamSessionById(Long examSessionId) {
        ExamSession session = examSessionRepository.findById(examSessionId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_SESSION_NOT_FOUND));
        return mapToResponse(session);
    }

    public List<ExamSessionResponse> getAllExamSessions(Long semesterId, Long schoolYearId) {
        List<ExamSession> sessions;
        if (semesterId != null && schoolYearId != null) {
            sessions = examSessionRepository.findBySemesterIdAndSchoolYearId(semesterId, schoolYearId);
        } else if (semesterId != null) {
            sessions = examSessionRepository.findBySemesterId(semesterId);
        } else if (schoolYearId != null) {
            sessions = examSessionRepository.findBySchoolYearId(schoolYearId);
        } else {
            sessions = examSessionRepository.findAll();
        }
        return sessions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private ExamSessionResponse mapToResponse(ExamSession session) {
        if (session == null) {
            return null;
        }
        SemesterResponse semesterResponse = schoolServiceClient.getSemesterById(session.getSemesterId()).getResult();
        SchoolYearResponse schoolYearResponse = schoolServiceClient.getSchoolYearById(session.getSchoolYearId()).getResult();
        return new ExamSessionResponse(
                session.getExamSessionId(),
                session.getSemesterId(),
                semesterResponse != null ? semesterResponse.getSemesterName() : null,
                session.getSchoolYearId(),
                schoolYearResponse != null ? schoolYearResponse.getSchoolYear() : null,
                session.getStartDate(),
                session.getEndDate()
        );
    }
}
