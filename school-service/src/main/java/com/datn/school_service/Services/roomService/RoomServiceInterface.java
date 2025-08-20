package com.datn.school_service.Services.roomService;

import com.datn.school_service.Dto.Request.RoomRequest;
import com.datn.school_service.Dto.Respone.BasicRoomResponse;
import com.datn.school_service.Dto.Respone.RoomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoomServiceInterface {
    Page<RoomResponse> getAll(Pageable pageable, boolean active);

    RoomResponse getRoomById(Long id);

    void createRoom(RoomRequest roomRequest);

    void updateRoom(Long id, RoomRequest roomRequest);

    void deleteRoom(Long id);

    List<RoomResponse> searchRooms(String keyword, Long roomFloor);


    List<RoomResponse> filterRoomsByPrefix(String Prefix);

    List<BasicRoomResponse> getRoomByIds(List<Long> ids);
}
