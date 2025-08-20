package com.datn.resource_service.service;

import com.datn.resource_service.dto.request.PresignedUrlForAvatarRequest;
import com.datn.resource_service.dto.request.PresignedUrlRequest;
import com.datn.resource_service.dto.request.UploadResourceRequest;
import com.datn.resource_service.dto.response.PresignedUrlForAvatarResponse;
import com.datn.resource_service.dto.response.PresignedUrlResponse;
import com.datn.resource_service.dto.response.ResourceResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ResourceService {
    ResourceResponse createResource(UploadResourceRequest request);

    PresignedUrlResponse generatePresignedUrl(String fileName, String fileType);

    ResourceResponse updateResource(Long id, UploadResourceRequest request);

    void deleteResource(Long id);

    List<PresignedUrlForAvatarResponse> getPresignedUrlsBatchForAvatarUpload(List<PresignedUrlForAvatarRequest> requests);

    List<PresignedUrlResponse> getPresignedUrlsBatch(List<PresignedUrlRequest> requests);

    Page<ResourceResponse> getAllResources(int page, int size, String sort, String direction);

    ResourceResponse getResourceById(Long id);

    Page<ResourceResponse> searchResources(int page, int size, String sort, String direction, String keyword);

    Page<ResourceResponse> getAllResourcesBySubjectId(Long subjectId, Long gradeId, int page, int size, String sort, String direction);
}
