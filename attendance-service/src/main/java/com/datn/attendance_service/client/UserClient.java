package com.datn.attendance_service.client;

import com.datn.attendance_service.dto.ApiResponse;
import com.datn.attendance_service.dto.user_service.StudentResponse;
import com.datn.attendance_service.dto.user_service.TeacherResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-service-client", url = "${application.client.user-service}/user")
public interface UserClient {
    @GetMapping("/{id}")
    ApiResponse<StudentResponse> getStudent(@PathVariable("id") String id);

    @GetMapping("/by-ids")
    ApiResponse<List<StudentResponse>> getUsersByIds(@RequestParam List<String> ids);

    @GetMapping("/get-all-teachers")
    ApiResponse<List<TeacherResponse>> getAllTeachers();

    @GetMapping("/teacher/{teacherId}")
    ApiResponse<TeacherResponse> getTeacherInfo(@PathVariable String teacherId);

}
