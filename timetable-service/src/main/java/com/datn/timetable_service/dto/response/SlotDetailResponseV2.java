package com.datn.timetable_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotDetailResponseV2 {
    private Long id;
    private Long classId;
    private Long subjectId;
    private String teacherId;
    private int dayOfWeek;
    private int slot;
    private Long roomId;
    private String mainSession; // MORNING or AFTERNOON
    private int week;
    private Long semesterId;
    private Long schoolYearId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
}
