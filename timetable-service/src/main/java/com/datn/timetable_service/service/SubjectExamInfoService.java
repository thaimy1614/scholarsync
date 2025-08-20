package com.datn.timetable_service.service;

import com.datn.timetable_service.client.SubjectServiceClient;
import com.datn.timetable_service.dto.request.SubjectExamInfoRequest;
import com.datn.timetable_service.dto.response.SubjectExamInfoResponse;
import com.datn.timetable_service.dto.subject_service.SubjectResponse;
import com.datn.timetable_service.exception.AppException;
import com.datn.timetable_service.exception.ErrorCode;
import com.datn.timetable_service.model.SubjectExamInfo;
import com.datn.timetable_service.repository.SubjectExamInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectExamInfoService {
    private final SubjectExamInfoRepository subjectExamInfoRepository;
    private final SubjectServiceClient subjectServiceClient;

    public SubjectExamInfoResponse createSubjectExamInfo(SubjectExamInfoRequest request) {
        if (subjectExamInfoRepository.existsBySubjectId(request.getSubjectId())) {
            throw new AppException(ErrorCode.EXAM_INFO_CONFLICT);
        }
        SubjectExamInfo examInfo = SubjectExamInfo.builder()
                .subjectId(request.getSubjectId())
                .duration(request.getDuration())
                .type(request.getType())
                .build();
        examInfo = subjectExamInfoRepository.save(examInfo);
        return mapToResponse(examInfo);
    }

    public SubjectExamInfoResponse updateSubjectExamInfo(Long id, SubjectExamInfoRequest request) {
        SubjectExamInfo examInfo = subjectExamInfoRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_INFO_NOT_FOUND));
        if (!examInfo.getSubjectId().equals(request.getSubjectId()) &&
                subjectExamInfoRepository.existsBySubjectId(request.getSubjectId())) {
            throw new AppException(ErrorCode.EXAM_INFO_CONFLICT);
        }
        examInfo.setSubjectId(request.getSubjectId());
        examInfo.setDuration(request.getDuration());
        examInfo.setType(request.getType());
        examInfo = subjectExamInfoRepository.save(examInfo);
        return mapToResponse(examInfo);
    }

    public void deleteSubjectExamInfo(Long id) {
        SubjectExamInfo examInfo = subjectExamInfoRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_INFO_NOT_FOUND));
        subjectExamInfoRepository.delete(examInfo);
    }

    public SubjectExamInfoResponse getSubjectExamInfoBySubjectId(Long subjectId) {
        SubjectExamInfo examInfo = subjectExamInfoRepository.findBySubjectId(subjectId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_INFO_NOT_FOUND));
        return mapToResponse(examInfo);
    }

    public List<SubjectExamInfoResponse> getAllSubjectExamInfo() {
        List<SubjectExamInfo> examInfos = subjectExamInfoRepository.findAll();
        List<Long> subjectIds = examInfos.stream()
                .map(SubjectExamInfo::getSubjectId)
                .toList();
        Map<Long, SubjectResponse> subjectMap = subjectServiceClient.getSubjectByIds(subjectIds)
                .getResult()
                .stream()
                .collect(Collectors.toMap(SubjectResponse::getId, subject -> subject));
        return examInfos.stream()
                .map(examInfo -> {
                    SubjectResponse subjectResponse = subjectMap.get(examInfo.getSubjectId());
                    return new SubjectExamInfoResponse(
                            examInfo.getId(),
                            examInfo.getSubjectId(),
                            subjectResponse.getName(),
                            examInfo.getDuration(),
                            examInfo.getType()
                    );
                })
                .collect(Collectors.toList());
    }

    private SubjectExamInfoResponse mapToResponse(SubjectExamInfo examInfo) {
        SubjectResponse subjectResponse = subjectServiceClient.getSubjectById(examInfo.getSubjectId()).getResult();
        return new SubjectExamInfoResponse(
                examInfo.getId(),
                examInfo.getSubjectId(),
                subjectResponse.getName(),
                examInfo.getDuration(),
                examInfo.getType()
        );
    }
}
