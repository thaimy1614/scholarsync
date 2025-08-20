package com.datn.school_service.Services.roomTypeService;

import com.datn.school_service.Dto.Request.AddRoomTypeRequest;
import com.datn.school_service.Dto.Request.SearchRoomTypeRequest;
import com.datn.school_service.Dto.Respone.RoomTypeResponse;
import com.datn.school_service.Exceptions.AppException;
import com.datn.school_service.Exceptions.ErrorCode;
import com.datn.school_service.Mapper.RoomTypeMapper;
import com.datn.school_service.Models.RoomType;
import com.datn.school_service.Repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomTypeService implements RoomTypeServiceInterface {

    final RoomTypeRepository roomTypeRepository;

    final RoomTypeMapper roomTypeMapper;

    @Override
    public Page<RoomTypeResponse> getAll(Pageable pageable, boolean active) {
        Page<RoomType> roomTypePage;
        if (active) {
            roomTypePage = roomTypeRepository.findAllByIsActiveTrue(pageable);
        } else {
            roomTypePage = roomTypeRepository.findAllByIsActiveFalse(pageable);
        }

        return roomTypePage.map(roomTypeMapper::toRoomTypeResponse);
    }

    @Override
    public RoomTypeResponse getRoomTypeById(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }

        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));

        return roomTypeMapper.toRoomTypeResponse(roomType);
    }

    @Override
    public void createRoomType(AddRoomTypeRequest addRoomTypeRequest) {
        if (addRoomTypeRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        boolean exists = roomTypeRepository.existsByRoomTypeName(addRoomTypeRequest.getRoomTypeName());
        if (exists) {
            throw new AppException(ErrorCode.ROOM_TYPE_NAME_ALREADY_EXIT);
        }
        RoomType roomtype = roomTypeMapper.toRoomType(addRoomTypeRequest);

        roomTypeRepository.save(roomtype);

    }

    @Override
    public void updateRoomType(Long id, AddRoomTypeRequest addRoomTypeRequest) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }

        RoomType existingRoomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));

        if (addRoomTypeRequest == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        if (!existingRoomType.getRoomTypeName().equals(addRoomTypeRequest.getRoomTypeName()) &&
                roomTypeRepository.existsByRoomTypeName(addRoomTypeRequest.getRoomTypeName())) {
            throw new AppException(ErrorCode.ROOM_TYPE_NAME_ALREADY_EXIT);
        }

        roomTypeMapper.toUpdateRoomType(existingRoomType, addRoomTypeRequest);
        roomTypeRepository.save(existingRoomType);
    }

    @Override
    public void deleteRoomType(Long id) {
        if (id == null) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        RoomType existingRoomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));
        if(!existingRoomType.isActive())
        {
            throw new AppException(ErrorCode.ROOM_TYPE_IS_DELETED);
        }
        existingRoomType.setActive(false);
        roomTypeRepository.save(existingRoomType);
    }

    @Override
    public List<RoomTypeResponse> searchRoom(SearchRoomTypeRequest keyword, boolean active) {
        if (keyword == null || keyword.getRoomTypeName() == null || keyword.getRoomTypeName().isBlank()) {
            throw new AppException(ErrorCode.EMPTY_REQUEST);
        }
        String newKeyword = keyword.getRoomTypeName().trim();
        List<RoomType> found;
        if (active) {
            found = roomTypeRepository.findAllByIsActiveTrueAndRoomTypeNameContainingIgnoreCase(newKeyword);
        } else {
            found = roomTypeRepository.findAllByIsActiveFalseAndRoomTypeNameContainingIgnoreCase(newKeyword);
        }
        if (found.isEmpty()) {
            throw new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND);
        }
        return found.stream()
                .map(roomTypeMapper::toRoomTypeResponse)
                .collect(Collectors.toList());
    }

}
