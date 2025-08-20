package com.datn.user_service.controller;

import com.datn.user_service.dto.ApiResponse;
import com.datn.user_service.dto.request.CheckComplaintRequest;
import com.datn.user_service.dto.request.ComplaintRequest;
import com.datn.user_service.dto.request.ResponseComplaintRequest;
import com.datn.user_service.dto.response.ComplaintResponse;
import com.datn.user_service.service.complaint.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${application.api.prefix}/complaints")
public class ComplaintController {
    private final ComplaintService complaintService;

    @GetMapping()
    ApiResponse<Page<ComplaintResponse>> getAllComplaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        return ApiResponse.<Page<ComplaintResponse>>builder()
                .message("Get all complaints successfully")
                .result(complaintService.getAllComplaints(page, size, sortBy, direction))
                .build();
    }

    @GetMapping("/search")
    ApiResponse<Page<ComplaintResponse>> searchComplaint(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam String keyword
    ) {
        return ApiResponse.<Page<ComplaintResponse>>builder()
                .message("Get all complaints successfully")
                .result(complaintService.searchComplaint(page, size, sortBy, direction, keyword))
                .build();
    }

    @GetMapping("/history")
    ApiResponse<Page<ComplaintResponse>> getComplaintHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            JwtAuthenticationToken jwt
    ) {
        String myId = jwt.getName();
        return ApiResponse.<Page<ComplaintResponse>>builder()
                .message("Get complaint history successfully")
                .result(complaintService.getMyComplaint(myId, page, size, sortBy, direction))
                .build();
    }

    @PostMapping("/send")
    ApiResponse<ComplaintResponse> createComplaint(
            @RequestBody ComplaintRequest complaint,
            JwtAuthenticationToken jwt
    ) {
        String myId = jwt.getName();
        return ApiResponse.<ComplaintResponse>builder()
                .message("Create complaint successfully")
                .result(complaintService.createComplaint(myId, complaint))
                .build();
    }

    @PostMapping("/check/{complaintId}")
    ApiResponse<ComplaintResponse> checkComplaint(
            @RequestBody CheckComplaintRequest request,
            @PathVariable Long complaintId
    ) {
        return ApiResponse.<ComplaintResponse>builder()
                .message("Check complaint successfully")
                .result(complaintService.checkComplaint(complaintId, request.getStatus().name()))
                .build();
    }

    @PostMapping("/response/{complaintId}")
    ApiResponse<ComplaintResponse> responseComplaint(
            @RequestBody ResponseComplaintRequest request,
            @PathVariable Long complaintId,
            JwtAuthenticationToken jwt
    ) {
        String responderId = jwt.getName();
        return ApiResponse.<ComplaintResponse>builder()
                .message("Response complaint successfully")
                .result(complaintService.responseComplaint(complaintId, request, responderId))
                .build();
    }
}
