package com.datn.school_service.Controllers;


import com.datn.school_service.Dto.Request.AddRoomTypeRequest;
import com.datn.school_service.Dto.Request.SearchRoomTypeRequest;
import com.datn.school_service.Dto.Respone.ApiResponse;
import com.datn.school_service.Dto.Respone.RoomTypeResponse;
import com.datn.school_service.Services.roomTypeService.RoomTypeServiceInterface;
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
@RequestMapping("${application.api.prefix}/room-types")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoomTypeController {

    private final RoomTypeServiceInterface roomTypeService;

    @GetMapping("/getAllRoomTypeActive")
    public ApiResponse<Page<RoomTypeResponse>> getAllRoomTypesActive(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "roomTypeName") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction

    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<RoomTypeResponse>>builder()
                .result(roomTypeService.getAll(pageable, true))
                .build();
    }

    @GetMapping("/getAllRoomTypeDelete")
    public ApiResponse<Page<RoomTypeResponse>> getAllRoomTypesDelete(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "roomTypeName") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction

    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<RoomTypeResponse>>builder()
                .result(roomTypeService.getAll(pageable, false))
                .build();
    }

    @GetMapping("/getRoomtypeById/{id}")
    public ApiResponse<RoomTypeResponse> getRoomTypeById(@PathVariable Long id) {
        return ApiResponse.<RoomTypeResponse>builder().result(roomTypeService.getRoomTypeById(id)).build();
    }

    @PostMapping("/addRoomType")
    public ApiResponse<Void> createRoomType(@RequestBody @Valid AddRoomTypeRequest addRoomTypeRequest) {
        roomTypeService.createRoomType(addRoomTypeRequest);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/editRoomType/{id}")
    public ApiResponse<Void> updateRoomType(
            @PathVariable Long id,
            @RequestBody @Valid AddRoomTypeRequest addRoomTypeRequest
    ) {
        roomTypeService.updateRoomType(id, addRoomTypeRequest);
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping("/DeleteRoomType/{id}")
    public ApiResponse<Void> deleteRoomType(@PathVariable Long id) {
        roomTypeService.deleteRoomType(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/searchRoomType/{active}")
    public ApiResponse<List<RoomTypeResponse>> searchRoomTypes(@RequestBody @Valid SearchRoomTypeRequest keyword, @PathVariable boolean active) {
        return ApiResponse.<List<RoomTypeResponse>>builder().result(roomTypeService.searchRoom(keyword, active)).build();
    }
}