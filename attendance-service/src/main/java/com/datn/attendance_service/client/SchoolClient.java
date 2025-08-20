package com.datn.attendance_service.client;

import com.datn.attendance_service.dto.ApiResponse;
import com.datn.attendance_service.dto.ClassResponse;
import com.datn.attendance_service.dto.RoomResponse;
import com.datn.attendance_service.dto.user_service.StudentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "school-service", url = "${application.client.school-service}/school")
public interface SchoolClient {
    @GetMapping("/class/get-class-by-id/{id}")
    ApiResponse<ClassResponse> getClassInfo(@PathVariable("id") Long id);

    @GetMapping("/room/getById/{id}")
    ApiResponse<RoomResponse> getRoomInfo(@PathVariable("id") Long id);

    @GetMapping("/class/get-list-student-by-class-id/{id}")
    ApiResponse<List<StudentResponse>> getListStudentByClassId(@PathVariable("id") Long id);
}