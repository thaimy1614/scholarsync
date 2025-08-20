package com.datn.user_service.service.student;

import com.datn.user_service.dto.response.StudentResponse;

import java.util.List;

public interface StudentService {
    List<StudentResponse> getAllStudents();
}
