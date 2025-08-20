package com.datn.user_service.service.student;

import com.datn.user_service.dto.response.StudentResponse;
import com.datn.user_service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;

    public List<StudentResponse> getAllStudents() {
        return null;
    }
}
