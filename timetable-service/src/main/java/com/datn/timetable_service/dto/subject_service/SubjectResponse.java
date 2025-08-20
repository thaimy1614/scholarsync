package com.datn.timetable_service.dto.subject_service;

import com.datn.timetable_service.dto.SchoolService.RoomResponse;
import lombok.Data;

@Data
public class SubjectResponse {
    private Long id;
    private String name;
    private int weeklySlots;
    private int semesterSlots;
    private int maxSlotsPerSession;
    private boolean preferConsecutive;
    private boolean isMainSubject;
    private Integer specialSlot;
    private RoomResponse specialRoom;
}
