package com.datn.school_service.Dto.Request.RoomType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddRoomTypeRequest {
    private String roomTypeDescription;

    private String roomTypeName;
}
