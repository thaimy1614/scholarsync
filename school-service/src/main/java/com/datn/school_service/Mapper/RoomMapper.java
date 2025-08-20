package com.datn.school_service.Mapper;

import com.datn.school_service.Dto.Request.RoomRequest;
import com.datn.school_service.Dto.Respone.RoomResponse;
import com.datn.school_service.Models.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = RoomTypeMapper.class)
public interface RoomMapper {
    @Mapping(source = "roomType", target = "roomTypeResponse")
    RoomResponse toRoomResponse(Room room);

    void roomUpdate(@MappingTarget Room room, RoomRequest roomRequest);

    Room toRoom(RoomRequest roomRequest);
}
