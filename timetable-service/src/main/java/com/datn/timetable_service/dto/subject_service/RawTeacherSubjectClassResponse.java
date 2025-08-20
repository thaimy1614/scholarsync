package com.datn.timetable_service.dto.subject_service;

import com.datn.timetable_service.dto.UserService.TeacherResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RawTeacherSubjectClassResponse {
    private String teacherId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private String gender;
    private Long subjectId;
    private String subjectName;
    private int weeklySlots;
    private int maxSlotsPerSession;
    private boolean preferConsecutive;
    private boolean isMainSubject;
    private Integer specialSlot;
    private Object specialRoom;
    private int schoolYearId;
    private Long classId;
    private String className;
    private TeacherResponse homeRoomTeacher;
}
