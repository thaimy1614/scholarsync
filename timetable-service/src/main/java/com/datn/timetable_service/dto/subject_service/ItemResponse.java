package com.datn.timetable_service.dto.subject_service;

import com.datn.timetable_service.dto.SchoolService.ClassResponse;
import com.datn.timetable_service.dto.UserService.TeacherResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ItemResponse {
    private List<TeacherSubjectClassResponse> items;
}
