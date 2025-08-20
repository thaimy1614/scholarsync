package com.datn.timetable_service.dto.SchoolService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomResponse {
     private Long roomId;
     private String roomName;
     private int roomFloor;
     private RoomTypeResponse roomType;
}
