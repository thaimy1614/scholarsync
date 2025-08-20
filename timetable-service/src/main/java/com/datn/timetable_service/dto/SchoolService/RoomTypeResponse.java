package com.datn.timetable_service.dto.SchoolService;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomTypeResponse {
    private Long roomTypeId;
    private String roomTypeName;
}
