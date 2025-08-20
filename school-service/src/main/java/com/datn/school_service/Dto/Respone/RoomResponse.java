package com.datn.school_service.Dto.Respone;

import com.datn.school_service.Models.RoomType;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {

    private Long roomId;

    private String roomName;

    private Long roomFloor;

    private Long numberOfChalkboard;

    private Long numberOfDevice;

    private boolean active;

    private RoomTypeResponse roomTypeResponse;
}
