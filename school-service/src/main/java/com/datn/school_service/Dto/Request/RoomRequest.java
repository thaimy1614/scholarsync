package com.datn.school_service.Dto.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoomRequest {

    @Column(name = "room_name", unique = true, length = 100)
    private String roomName;

    @Column(name = "room_floor", length = 100)
    private Long roomFloor;

    @Column(name = "number_of_chalkboard")
    private Long numberOfChalkboard;

    private long roomTypeId;

    @Column(name = "number_of_device")
    private Long numberOfDevice;
    @Column(name = "is_active")
    private boolean active;
}
