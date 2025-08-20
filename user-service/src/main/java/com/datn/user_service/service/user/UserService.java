package com.datn.user_service.service.user;

import com.datn.user_service.dto.request.RegisterUser;
import com.datn.user_service.dto.response.*;
import com.datn.user_service.model.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    Object getMyInfo(String userId);

    Object updateMyInfo(String userId, RegisterUser userRequest);

    List<GetUserFullNameResponse> getFullName(List<String> userId);

    CountUserResponse countUsers();

    Page<?> searchUsers(String key, String role, Pageable pageable);

    Page<UserResponse> getAllUsers(int page, int size, String sortBy, String direction);

    Page<StudentResponse> getAllStudents(int page, int size, String sortBy, String direction);

    Page<TeacherResponse> getAllTeachers(int page, int size, String sortBy, String direction);

    Page<ParentResponse> getAllParents(int page, int size, String sortBy, String direction);

    List<TeacherResponse> getAllTeachers();

    TeacherResponse getTeacherInfo(String userId);

    List<TeacherResponse> getTeachersInfo(List<String> ids);

    List<Object> getUsersByIds(List<String> ids);

    void blockUsers(List<String> ids);

    void unblockUsers(List<String> ids);

    void deleteUsers(List<String> ids);

    StudentResponse setDiscipleMonitor(String userId);

    StudentResponse getStudentInfo(String userId);

    Page<TeacherResponse> getAllPrincipals(int page, int size, String sortBy, String direction);

    Page<StudentResponse> getAllMonitor(int page, int size, String sortBy, String direction);
}
