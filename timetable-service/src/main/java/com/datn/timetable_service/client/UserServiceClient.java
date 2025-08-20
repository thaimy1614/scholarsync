package com.datn.timetable_service.client;

import com.datn.timetable_service.dto.ApiResponse;
import com.datn.timetable_service.dto.UserService.TeacherResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-service", url = "${application.clients.user-service}/user")
public interface UserServiceClient {
    @GetMapping("/get-all-teachers")
    ApiResponse<List<TeacherResponse>> getAllTeachers();

    @GetMapping("/teacher/{teacherId}")
    ApiResponse<TeacherResponse> getTeacherInfo(@PathVariable String teacherId);

    @GetMapping("/teacher/bulk")
    ApiResponse<List<TeacherResponse>> getTeachersInfo(@RequestParam List<String> ids);
}
