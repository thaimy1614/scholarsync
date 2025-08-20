package com.datn.timetable_service.dto.subject_service;

import com.datn.timetable_service.dto.SchoolService.ClassResponse;
import com.datn.timetable_service.dto.UserService.TeacherResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeacherSubjectClassResponse {
    private TeacherResponse teacher;
    private SubjectResponse subject;
    private ClassResponse clazz;
}
