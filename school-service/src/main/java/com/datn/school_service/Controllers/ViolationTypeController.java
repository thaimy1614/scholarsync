package com.datn.school_service.Controllers;

import com.datn.school_service.Dto.Request.ViolationType.AddViolationTypeRequest;
import com.datn.school_service.Dto.Request.ViolationType.SearchViolationTypeRequest;
import com.datn.school_service.Dto.Respone.ViolationType.ViolationTypeResponse;
import com.datn.school_service.Dto.Respone.ApiResponse;
import com.datn.school_service.Services.ViolationType.ViolationTypeServiceInterface;
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
@RequestMapping("${application.api.prefix}/violation-type")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ViolationTypeController {
    private final ViolationTypeServiceInterface violationtypeService;

    @GetMapping("/getAllViolationTypeActive")
    public ApiResponse<Page<ViolationTypeResponse>> getAllViolationTypesActive(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "ViolationTypeName") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction

    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<ViolationTypeResponse>>builder()
                .result(violationtypeService.getAll(pageable, true))
                .build();
    }

    @GetMapping("/getAllViolationTypeDelete")
    public ApiResponse<Page<ViolationTypeResponse>> getAllViolationTypesDelete(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "ViolationTypeName") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction

    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<ViolationTypeResponse>>builder()
                .result(violationtypeService.getAll(pageable, false))
                .build();
    }

    @GetMapping("/getViolationTypeById/{id}")
    public ApiResponse<ViolationTypeResponse> getViolationTypeById(@PathVariable Long id) {
        return ApiResponse.<ViolationTypeResponse>builder().result(violationtypeService.getViolationTypeById(id)).build();
    }

    @PostMapping("/addViolationType")
    public ApiResponse<Void> createViolationType(@RequestBody @Valid AddViolationTypeRequest addViolationTypeRequest) {
        violationtypeService.createViolationType(addViolationTypeRequest);
        return ApiResponse.<Void>builder().build();
    }


    @PutMapping("/editViolationType/{id}")
    public ApiResponse<Void> updateViolationType(
            @PathVariable Long id,
            @RequestBody @Valid  AddViolationTypeRequest addViolationTypeRequest
    ) {
        violationtypeService.updateViolationType(id, addViolationTypeRequest);
        return ApiResponse.<Void>builder().build();

    }

    @DeleteMapping("/DeleteViolationType/{id}")
    public ApiResponse<Void> deleteViolationType(@PathVariable Long id) {
        violationtypeService.deleteViolationType(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/searchViolationType/{active}")
    public ApiResponse<List<ViolationTypeResponse>> searchViolationTypes(@RequestBody @Valid SearchViolationTypeRequest keyword, @PathVariable boolean active) {
        return ApiResponse.<List<ViolationTypeResponse>>builder().result(violationtypeService.searchViolationType(keyword, active)).build();
    }
}
