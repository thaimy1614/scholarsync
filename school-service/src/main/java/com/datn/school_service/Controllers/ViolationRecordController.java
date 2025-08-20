//package com.datn.school_service.Controllers;
//
//import com.datn.school_service.Dto.Request.ViolationRecord.AddViolationRecordRequest;
//import com.datn.school_service.Dto.Request.ViolationType.AddViolationTypeRequest;
//import com.datn.school_service.Dto.Respone.ApiResponse;
//import com.datn.school_service.Dto.Respone.ViolationRecord.ViolationRecordResponse;
//import com.datn.school_service.Services.ViolationRecord.ViolationRecordService;
//import com.datn.school_service.Services.ViolationType.ViolationTypeServiceInterface;
//import jakarta.validation.Valid;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Comparator;
//import java.util.List;
//
//@RestController
//@RequestMapping("${application.api.prefix}/violation-record")
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//@Slf4j
//public class ViolationRecordController {
//    private final ViolationRecordService violationRecordService;
//
//    @PostMapping("/addViolationRecord")
//    public ApiResponse<Void> createViolationRecord(@RequestBody @Valid AddViolationRecordRequest addViolationRecordRequest) {
//        violationRecordService.createViolationRecord(addViolationRecordRequest);
//        return ApiResponse.<Void>builder().build();
//    }
//    @GetMapping("/get-violation-record-by-id/{id}")
//    public ApiResponse<ViolationRecordResponse> getViolationRecordById(@PathVariable Long id) {
//        ViolationRecordResponse result = violationRecordService.getViolationRecordById(id);
//        return ApiResponse.<ViolationRecordResponse>builder()
//                .result(result)
//                .build();
//    }
//    @GetMapping("/get-all")
//    public ApiResponse<Page<ViolationRecordResponse>> getAllViolationRecords(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "createdAt") String sort,
//            @RequestParam(defaultValue = "desc") String direction,
//            @RequestParam(defaultValue = "true") boolean active
//    ) {
//        Pageable pageable = PageRequest.of(page, size);
//        Page<ViolationRecordResponse> result = violationRecordService.getAll(pageable, active);
//
//        // Nếu cần sort theo className (vì không sort được từ DB)
//        if (sort.equalsIgnoreCase("className")) {
//            Comparator<ViolationRecordResponse> comparator = Comparator.comparing(
//                    ViolationRecordResponse::getClassName,
//                    Comparator.nullsLast(String::compareToIgnoreCase)
//            );
//
//            if (direction.equalsIgnoreCase("desc")) {
//                comparator = comparator.reversed();
//            }
//
//            List<ViolationRecordResponse> sortedList = result.getContent().stream()
//                    .sorted(comparator)
//                    .toList();
//
//            result = new PageImpl<>(sortedList, pageable, result.getTotalElements());
//        }
//
//        return ApiResponse.<Page<ViolationRecordResponse>>builder()
//                .result(result)
//                .build();
//    }
//
//    @PutMapping("/update/{id}")
//    public ApiResponse<Void> updateViolationRecord(
//            @PathVariable Long id,
//            @RequestBody @Valid AddViolationRecordRequest request
//    ) {
//        violationRecordService.updateViolationRecord(id, request);
//        return ApiResponse.<Void>builder().build();
//    }
//    @DeleteMapping("/delete/{id}")
//    public ApiResponse<Void> deleteViolationRecord(@PathVariable Long id) {
//        violationRecordService.deleteViolationRecord(id);
//        return ApiResponse.<Void>builder().build();
//    }
//    @GetMapping("/get-by-class-id/{classId}")
//    public ApiResponse<List<ViolationRecordResponse>> getByClassId(@PathVariable Long classId) {
//        List<ViolationRecordResponse> result = violationRecordService.getViolationRecordByClassId(classId);
//        return ApiResponse.<List<ViolationRecordResponse>>builder()
//                .result(result)
//                .build();
//    }
//
//
//}
