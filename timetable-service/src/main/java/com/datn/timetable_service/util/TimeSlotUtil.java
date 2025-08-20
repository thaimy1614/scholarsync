package com.datn.timetable_service.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeSlotUtil {
//    private static final int SLOTS_PER_DAY = 10;
//    private static final LocalTime MORNING_START = LocalTime.of(7, 0);
//    private static final LocalTime AFTERNOON_START = LocalTime.of(13, 0);
//    private static final int SLOT_DURATION_MINUTES = 45;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final LocalTime[] START_TIMES = {
            LocalTime.of(7, 0), LocalTime.of(7, 45), LocalTime.of(8, 50), LocalTime.of(9, 35), LocalTime.of(10, 25),
            LocalTime.of(13, 0), LocalTime.of(13, 45), LocalTime.of(14, 50), LocalTime.of(15, 35), LocalTime.of(16, 25)
    };
    private static final LocalTime[] END_TIMES = {
            LocalTime.of(7, 45), LocalTime.of(8, 30), LocalTime.of(9, 35), LocalTime.of(10, 20), LocalTime.of(11, 10),
            LocalTime.of(13, 45), LocalTime.of(14, 30), LocalTime.of(15, 35), LocalTime.of(16, 20), LocalTime.of(17, 10)
    };

    public static LocalTime getStartTime(int slotInDay) {
        return START_TIMES[(slotInDay - 1) % 10];
    }

    public static LocalTime getEndTime(int slotInDay) {
        return END_TIMES[(slotInDay - 1) % 10];
    }
}