package com.datn.school_service.Dto.Respone;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BasicRoomResponse {
    private Long roomId;
    private String roomName;
}
