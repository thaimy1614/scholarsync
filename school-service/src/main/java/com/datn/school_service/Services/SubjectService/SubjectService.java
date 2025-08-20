package com.datn.school_service.Services.SubjectService;

import com.datn.school_service.Dto.Respone.Subject.SubjectNameResponse;
import com.datn.school_service.Exceptions.AppException;
import com.datn.school_service.Exceptions.ErrorCode;
import com.datn.school_service.HttpClient.SubjectServiceClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubjectService {
    private final SubjectServiceClient subjectServiceClient;
    private final ObjectMapper objectMapper;
    public SubjectNameResponse getSubjectName(Long id) {
        var response = subjectServiceClient.GetSubjectById(id);

        if (response == null || response.getResult() == null)
        {  throw new AppException(ErrorCode.CALL_SERVICE_FALL);}

        return objectMapper.convertValue(response.getResult(), SubjectNameResponse.class);
    }

    public boolean checkTeacherTeachesSubjectInClass(String teacherId, Long subjectId, Long classId) {
        var response = subjectServiceClient.checkTeacherSubjectClassExists(teacherId, subjectId, classId);

        if (response == null || response.getResult() == null) {
            throw new AppException(ErrorCode.CALL_SERVICE_FALL, "checkTeacherSubjectClassExists");
        }

        return response.getResult(); // kết quả true hoặc false
    }




}
