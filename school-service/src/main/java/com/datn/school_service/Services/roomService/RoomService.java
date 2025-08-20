package com.datn.school_service.Services.roomService;

import com.datn.school_service.Dto.Request.RoomRequest;
import com.datn.school_service.Dto.Respone.BasicRoomResponse;
import com.datn.school_service.Dto.Respone.RoomResponse;
import com.datn.school_service.Exceptions.AppException;
import com.datn.school_service.Exceptions.ErrorCode;
import com.datn.school_service.Mapper.RoomMapper;
import com.datn.school_service.Mapper.RoomTypeMapper;
import com.datn.school_service.Models.Room;
import com.datn.school_service.Models.RoomType;
import com.datn.school_service.Repository.RoomRepository;
import com.datn.school_service.Repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class RoomService implements RoomServiceInterface {

    final RoomRepository roomRepository;
    final RoomMapper roomMapper;
    final RoomTypeMapper roomTypeMapper;
    final RoomTypeRepository roomTypeRepository;



    @Override
    public Page<RoomResponse> getAll(Pageable pageable, boolean active) {
        Page<Room> rooms;
        if (active) {
            rooms = roomRepository.findAllByActiveTrue(pageable);
        } else {
            rooms = roomRepository.findAllByActiveFalse(pageable);
        }

        return rooms.map(roomMapper::toRoomResponse);
    }

    @Override
    public RoomResponse getRoomById(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITYS_NOT_FOUND, "Room", id));

        return roomMapper.toRoomResponse(room);
    }


    @Override
    public void createRoom(RoomRequest roomRequest) {
        if (roomRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        boolean exists = roomRepository.existsByRoomName(roomRequest.getRoomName());
        if (exists) {
            throw new AppException(ErrorCode.ROOM_NAME_ALREADY_EXIT);
        }
        RoomType roomType = roomTypeRepository.findById(roomRequest.getRoomTypeId()).orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));

        Room room = roomMapper.toRoom(roomRequest);
        room.setRoomType(roomType);
        try {
            roomRepository.save(room);
        } catch (Exception e) {
            throw new AppException(ErrorCode.ROOM_NAME_ALREADY_EXIT);
        }
    }


    @Override
    public void updateRoom(Long id, RoomRequest roomRequest) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }

        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        if (roomRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        if (!existingRoom.getRoomName().equals(roomRequest.getRoomName()) &&
                roomRepository.existsByRoomName(roomRequest.getRoomName())) {
            throw new AppException(ErrorCode.ROOM_NAME_ALREADY_EXIT);
        }
        RoomType roomType = roomTypeRepository.findById(roomRequest.getRoomTypeId()).orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));


        roomMapper.roomUpdate(existingRoom, roomRequest);
        existingRoom.setRoomType(roomType);
        roomRepository.save(existingRoom);
    }


    @Override
    public void deleteRoom(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        Room room = roomRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        if (!room.isActive()) {
            throw new AppException(ErrorCode.ROOM_IS_DELETED);
        }
        room.setActive(false);
        roomRepository.save(room);
    }

    @Override
    public List<RoomResponse> searchRooms(String keyword, Long roomFloor) {
        if ((keyword == null || keyword.isEmpty()) && roomFloor == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Keyword and room floor cannot both be null");
        }
        if (roomRepository.count() == 0) {
            throw new AppException(ErrorCode.ENTITY_EMPTY, "Rooms");
        }
        List<Room> rooms = roomRepository.findByActiveTrueAndRoomNameContainingIgnoreCaseOrRoomFloor(keyword, roomFloor);
        if (rooms.isEmpty()) {
            throw new AppException(ErrorCode.ENTITY_EMPTY, "Rooms");
        }
        return rooms.stream().map(roomMapper::toRoomResponse).collect(Collectors.toList());
    }

    @Override
    public List<RoomResponse> filterRoomsByPrefix(String Prefix) {
        String namePrefix = Prefix.trim();

        if (namePrefix.isEmpty()) {
            throw new AppException(ErrorCode.INPUT_NULL, "Prefix cannot be null or empty");
        }
        if (namePrefix.length() != 1) {
            throw new AppException(ErrorCode.INPUT_INVALID, "prefix format just only one character");
        }
        List<Room> rooms = roomRepository.findAllByActiveTrue();
        if (rooms.isEmpty()) {
            throw new AppException(ErrorCode.ENTITY_EMPTY, "Rooms");
        }
        String PrefixName = namePrefix.toUpperCase();

        List<Room> filteredRooms = rooms.stream()
                .filter(room -> (PrefixName == null || room.getRoomName().startsWith(namePrefix)))
                .collect(Collectors.toList());

        return filteredRooms.stream()
                .map(roomMapper::toRoomResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BasicRoomResponse> getRoomByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }

        List<Room> rooms = roomRepository.findAllById(ids);
        if (rooms.isEmpty()) {
            throw new AppException(ErrorCode.ENTITYS_NOT_FOUND, "Rooms", ids.toString());
        }

        return rooms.stream()
                .map(room -> BasicRoomResponse.builder()
                        .roomId(room.getRoomId())
                        .roomName(room.getRoomName())
                        .build())
                .collect(Collectors.toList());
    }
}
