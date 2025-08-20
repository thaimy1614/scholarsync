package com.datn.school_service.Mapper;

import com.datn.school_service.Dto.Request.AddRoomTypeRequest;
import com.datn.school_service.Dto.Request.UpdateNewTypeRequest;
import com.datn.school_service.Dto.Respone.RoomTypeResponse;
import com.datn.school_service.Models.RoomType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
@Mapper(componentModel = "spring")
public interface RoomTypeMapper {
    RoomType toRoomType(AddRoomTypeRequest addRoomTypeRequest);


    RoomTypeResponse toRoomTypeResponse(RoomType roomType);

    void toUpdateRoomType(@MappingTarget RoomType roomType,AddRoomTypeRequest addRoomTypeRequest);

    RoomType toRoomTypeUpdate(UpdateNewTypeRequest updateNewTypeRequest);
}
