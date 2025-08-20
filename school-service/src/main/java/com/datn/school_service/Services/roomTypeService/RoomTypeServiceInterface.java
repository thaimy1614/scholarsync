package com.datn.school_service.Services.roomTypeService;

import com.datn.school_service.Dto.Request.AddRoomTypeRequest;
import com.datn.school_service.Dto.Request.SearchRoomTypeRequest;
import com.datn.school_service.Dto.Respone.RoomTypeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoomTypeServiceInterface {
    Page<RoomTypeResponse> getAll(Pageable pageable, boolean active);

    RoomTypeResponse getRoomTypeById(Long id);

    void createRoomType(AddRoomTypeRequest addRoomTypeRequest);

    void updateRoomType(Long id, AddRoomTypeRequest addRoomTypeRequest );

    void deleteRoomType(Long id);

    List<RoomTypeResponse> searchRoom(SearchRoomTypeRequest keyword, boolean active);
}
