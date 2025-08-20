package com.datn.timetable_service.dto.SchoolService;

import com.datn.timetable_service.dto.UserService.TeacherResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClassResponse {
    private Long classId;
    private String className;
    private int schoolYearId;
    private String schoolYear;
    private String mainSession;
    private TeacherResponse teacher;
    private RoomResponse roomResponse;
}
