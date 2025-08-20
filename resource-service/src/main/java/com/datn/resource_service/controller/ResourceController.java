package com.datn.resource_service.controller;

import com.datn.resource_service.dto.ApiResponse;
import com.datn.resource_service.dto.request.PresignedUrlForAvatarRequest;
import com.datn.resource_service.dto.request.PresignedUrlRequest;
import com.datn.resource_service.dto.request.UploadResourceRequest;
import com.datn.resource_service.dto.response.PresignedUrlForAvatarResponse;
import com.datn.resource_service.dto.response.PresignedUrlResponse;
import com.datn.resource_service.dto.response.ResourceResponse;
import com.datn.resource_service.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${application.api.prefix}")
public class    ResourceController {
    private final ResourceService resourceService;

    @GetMapping("/get-all")
    ApiResponse<Page<ResourceResponse>> getAllResources(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "resourceName") String sort,
            @RequestParam(value = "search", required = false, defaultValue = "asc") String direction

    ) {
        Page<ResourceResponse> resourceResponses = resourceService.getAllResources(page, size, sort, direction);
        return ApiResponse.<Page<ResourceResponse>>builder()
                .result(resourceResponses)
                .build();
    }

    @GetMapping("/by-id/{id}")
    ApiResponse<ResourceResponse> getResourceById(@PathVariable Long id){
        return ApiResponse.<ResourceResponse>builder()
                .result(resourceService.getResourceById(id))
                .build();
    }

    @PostMapping()
    ApiResponse<ResourceResponse> createResource(
            @Valid @RequestBody UploadResourceRequest uploadResourceRequest
    ) {
        ResourceResponse resourceResponse = resourceService.createResource(uploadResourceRequest);
        return ApiResponse.<ResourceResponse>builder()
                .result(resourceResponse)
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<ResourceResponse> updateResource(
            @PathVariable Long id,
            @Valid @RequestBody UploadResourceRequest uploadResourceRequest
    ) {
        ResourceResponse resourceResponse = resourceService.updateResource(id, uploadResourceRequest);
        return ApiResponse.<ResourceResponse>builder()
                .result(resourceResponse)
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<Boolean> deleteResource(
            @PathVariable Long id
    ) {
        resourceService.deleteResource(id);
        return ApiResponse.<Boolean>builder()
                .result(true)
                .build();
    }

    @GetMapping("/search")
    ApiResponse<Page<ResourceResponse>> searchResources(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "resourceName") String sort,
            @RequestParam(value = "direction", required = false, defaultValue = "asc") String direction,
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword
    ) {
        Page<ResourceResponse> resourceResponses = resourceService.searchResources(page, size, sort, direction, keyword);
        return ApiResponse.<Page<ResourceResponse>>builder()
                .result(resourceResponses)
                .build();
    }

    @GetMapping("/presigned-url")
    ApiResponse<PresignedUrlResponse> getPresignedUrls(
            @RequestParam String fileName,
            @RequestParam String fileType
    ) {
        PresignedUrlResponse response = resourceService.generatePresignedUrl(fileName, fileType);
        return ApiResponse.<PresignedUrlResponse>builder()
                .result(response)
                .build();
    }

    @PostMapping("/presigned-urls")
    public ApiResponse<List<PresignedUrlResponse>> getPresignedUrlsBatch(
            @RequestBody List<PresignedUrlRequest> requests) {

        List<PresignedUrlResponse> responses = resourceService.getPresignedUrlsBatch(requests);
        return ApiResponse.<List<PresignedUrlResponse>>builder()
                .result(responses)
                .build();
    }

    @PostMapping("/presigned-urls/avatar")
    ApiResponse<List<PresignedUrlForAvatarResponse>> getPresignedUrlsForAvatarUpload(@RequestBody List<PresignedUrlForAvatarRequest> requests) {
        List<PresignedUrlForAvatarResponse> responses = resourceService.getPresignedUrlsBatchForAvatarUpload(requests);
        return ApiResponse.<List<PresignedUrlForAvatarResponse>>builder()
                .result(responses)
                .build();
    }

    @GetMapping("/get-all-by-subject-and-grade")
    ApiResponse<Page<ResourceResponse>> getAllResourcesBySubjectId(
            @RequestParam(value = "subjectId") Long subjectId,
            @RequestParam(value = "gradeId") Long gradeId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "resourceName") String sort,
            @RequestParam(value = "direction", required = false, defaultValue = "asc") String direction
    ) {
        Page<ResourceResponse> resourceResponses = resourceService.getAllResourcesBySubjectId(subjectId, gradeId, page, size, sort, direction);
        return ApiResponse.<Page<ResourceResponse>>builder()
                .result(resourceResponses)
                .build();
    }

}
