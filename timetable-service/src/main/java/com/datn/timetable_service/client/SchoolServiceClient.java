package com.datn.timetable_service.client;

import com.datn.timetable_service.dto.ApiResponse;
import com.datn.timetable_service.dto.SchoolService.ClassResponse;
import com.datn.timetable_service.dto.SchoolService.RoomResponse;
import com.datn.timetable_service.dto.SchoolService.SchoolYearResponse;
import com.datn.timetable_service.dto.SchoolService.SemesterResponse;
import jakarta.xml.bind.UnmarshallerHandler;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "school-service", url = "${application.clients.school-service}/school")
public interface SchoolServiceClient {

    @GetMapping("/class/get-class-by-school-year-id/{id}")
    ApiResponse<List<ClassResponse>> getClassBySchoolYear(@PathVariable Long id);

    @GetMapping("/room/getAllRoomIsActive")
    ApiResponse<Page<RoomResponse>> getAllRoomIsActive(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam String sort,
            @RequestParam String direction
    );

    @GetMapping("/semester/get-semester-by-id/{id}")
    ApiResponse<SemesterResponse> getSemesterById(@PathVariable Long id);

    @GetMapping("/school-year/getSchoolYearById/{id}")
    ApiResponse<SchoolYearResponse> getSchoolYearById(@PathVariable Long id);

    @GetMapping("/class/get-class-by-id/{id}")
    ApiResponse<ClassResponse> getClassById(@PathVariable Long id);

    @GetMapping("/room/getById/{id}")
    ApiResponse<RoomResponse> getRoomById(@PathVariable Long id);

    @GetMapping("/class/get-class-by-ids")
    ApiResponse<List<ClassResponse>> getClassByIds(@RequestParam List<Long> ids);

    @GetMapping("/room/get-room-by-ids")
    ApiResponse<List<RoomResponse>> getRoomByIds(@RequestParam List<Long> ids);
}
