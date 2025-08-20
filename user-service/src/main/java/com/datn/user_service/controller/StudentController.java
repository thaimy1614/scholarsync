package com.datn.user_service.controller;

import com.datn.user_service.dto.ApiResponse;
import com.datn.user_service.dto.response.StudentResponse;
import com.datn.user_service.service.student.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/student")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;

    @GetMapping
    ApiResponse<List<StudentResponse>> getAllStudents() {
        List<StudentResponse> studentResponse = studentService.getAllStudents();
        return ApiResponse.<List<StudentResponse>>builder()
                .message("Get info successfully!")
                .result(studentResponse)
                .build();
    }
}
