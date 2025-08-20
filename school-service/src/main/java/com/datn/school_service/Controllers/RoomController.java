package com.datn.school_service.Controllers;

import com.datn.school_service.Dto.Request.RoomRequest;
import com.datn.school_service.Dto.Respone.ApiResponse;
import com.datn.school_service.Dto.Respone.BasicRoomResponse;
import com.datn.school_service.Dto.Respone.RoomResponse;
import com.datn.school_service.Services.roomService.RoomServiceInterface;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${application.api.prefix}/room")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoomController {
    final RoomServiceInterface roomServiceInterface;

    @GetMapping("/getAllRoomIsActive")
    public ApiResponse<Page<RoomResponse>> getAllRoomsActive(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "roomFloor") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction
    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<RoomResponse>>builder()
                .result(roomServiceInterface.getAll(pageable,true))
                .build();
    }
    @GetMapping("/getAllRoomIsDelete")
    public ApiResponse<Page<RoomResponse>> getAllRoomsDelete(@RequestParam(value = "page", defaultValue = "0") int page,
                                                             @RequestParam(value = "size", defaultValue = "10") int size,
                                                             @RequestParam(value = "sort", defaultValue = "roomFloor") String sort,
                                                             @RequestParam(value = "direction", defaultValue = "asc") String direction
    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<RoomResponse>>builder()
                .result(roomServiceInterface.getAll(pageable,false))
                .build();
    }
    @GetMapping("/getById/{id}") //ko phân biệt xóa hay chưa
    public ApiResponse<RoomResponse> getRoomById(@PathVariable Long id)
    {
        return ApiResponse.<RoomResponse>builder()
                .result(roomServiceInterface.getRoomById(id))
                .build();
    }
    @PostMapping("/addRoom")
    public ApiResponse<Void> createRoom (@Valid @RequestBody RoomRequest roomRequest) {
        roomServiceInterface.createRoom(roomRequest);
        return ApiResponse.<Void>builder().build();
    }
    @PutMapping("editRoom/{id}")
    public ApiResponse<Void> editRoom(@Valid @PathVariable Long id, @RequestBody RoomRequest roomRequest) {
        roomServiceInterface.updateRoom(id, roomRequest);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/deleteRoom/{id}")
    public ApiResponse<Void> deleteRoom (@RequestParam Long id) {
        roomServiceInterface.deleteRoom(id);
        return ApiResponse.<Void>builder().build();
    }
    @GetMapping("/searchRoom")
    public ApiResponse<List<RoomResponse>> searchRooms(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long roomFloor) {
        return ApiResponse.<List<RoomResponse>>builder()
                .result(roomServiceInterface.searchRooms(keyword, roomFloor))
                .build();
    }
    @GetMapping("filterRoom/{namePrefix}")
    public ApiResponse<List<RoomResponse>> filterRooms(@RequestParam String namePrefix)
    {
        return ApiResponse.<List<RoomResponse>>builder()
                .result(roomServiceInterface.filterRoomsByPrefix(namePrefix))
                .build();
    }

    @GetMapping("/get-room-by-ids")
    public ApiResponse<List<BasicRoomResponse>> getRoomByIds(@RequestParam List<Long> ids) {
        return ApiResponse.<List<BasicRoomResponse>>builder()
                .result(roomServiceInterface.getRoomByIds(ids))
                .build();
    }
}
