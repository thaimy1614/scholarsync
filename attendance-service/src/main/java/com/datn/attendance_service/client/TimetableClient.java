package com.datn.attendance_service.client;

import com.datn.attendance_service.dto.ApiResponse;
import com.datn.attendance_service.dto.timetable_service.SlotDetailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "timetable-service", url = "${application.client.timetable-service}/timetable")
public interface TimetableClient {
    @GetMapping("/{id}/details-for-other-services")
    ApiResponse<SlotDetailResponse> getTimetable(@PathVariable("id") Long id);

    @GetMapping("/details-for-other-services/by-ids")
    ApiResponse<List<SlotDetailResponse>> getTimetableByIds(@RequestParam List<Long> ids);

    @GetMapping("/get-all")
    ApiResponse<List<SlotDetailResponse>> getAllTimetableSlots();
}