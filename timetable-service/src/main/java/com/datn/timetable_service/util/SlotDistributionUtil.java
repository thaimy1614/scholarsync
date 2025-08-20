package com.datn.timetable_service.util;

import java.util.ArrayList;
import java.util.List;

public class SlotDistributionUtil {
    public static List<Integer> distributeSlots(int totalSlots, int totalWeeks) {
        List<Integer> weeklySlots = new ArrayList<>();
        int baseSlots = totalSlots / totalWeeks; // Số slot cơ bản mỗi tuần
        int remainingSlots = totalSlots % totalWeeks; // Số slot dư

        for (int week = 0; week < totalWeeks; week++) {
            if (week < remainingSlots) {
                weeklySlots.add(baseSlots + 1); // Phân bổ thêm 1 slot cho các tuần đầu
            } else {
                weeklySlots.add(baseSlots); // Các tuần sau nhận số slot cơ bản
            }
        }
        return weeklySlots;
    }
}
