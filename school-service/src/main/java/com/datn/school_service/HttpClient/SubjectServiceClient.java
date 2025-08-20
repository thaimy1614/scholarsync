package com.datn.school_service.HttpClient;

import com.datn.school_service.Dto.Respone.ApiResponse;
import com.datn.school_service.Dto.Respone.User.GetUserNameResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;

@FeignClient(name = "subject-service-client", url = "${application.subject-service}/subject")
public interface SubjectServiceClient {

//    "get-subject-by/subjectId/{subjectId}

    @GetMapping("/get-subject-by/subjectId/{subjectId}")
    ApiResponse<Object> GetSubjectById(@PathVariable Long subjectId);

    @GetMapping("/verify-class-existence-by/teacherId/{teacherId}")
    ApiResponse<Object> verifyClassExistenceByTeacher(
            @PathVariable("teacherId") String teacherId,
            @RequestParam("classIds") List<Long> classIds

    );
    @GetMapping("/teacher-subject-class/check-teacher-subject-class-exists/teacherId/{teacherId}/subjectId/{subjectId}/classId/{classId}")
    ApiResponse<Boolean> checkTeacherSubjectClassExists(
            @PathVariable("teacherId") String teacherId,
            @PathVariable("subjectId") Long subjectId,
            @PathVariable("classId") Long classId
    );


}
