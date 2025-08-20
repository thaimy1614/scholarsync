package com.datn.resource_service.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.datn.resource_service.client.SchoolClient;
import com.datn.resource_service.client.SubjectClient;
import com.datn.resource_service.client.UserClient;
import com.datn.resource_service.dto.request.PresignedUrlForAvatarRequest;
import com.datn.resource_service.dto.request.PresignedUrlRequest;
import com.datn.resource_service.dto.request.UploadResourceRequest;
import com.datn.resource_service.dto.response.PresignedUrlForAvatarResponse;
import com.datn.resource_service.dto.response.PresignedUrlResponse;
import com.datn.resource_service.dto.response.ResourceResponse;
import com.datn.resource_service.exception.AppException;
import com.datn.resource_service.exception.ErrorCode;
import com.datn.resource_service.mapper.ResourceMapper;
import com.datn.resource_service.model.Resource;
import com.datn.resource_service.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    private final ResourceRepository resourceRepository;
    private final AmazonS3 s3;
    private final ResourceMapper resourceMapper;
    private final UserClient userClient;
    private final SubjectClient subjectClient;
    private final SchoolClient schoolClient;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    private String generateUniqueFileName(String fileName) {
        return UUID.randomUUID() + "-" + fileName;
    }

    public PresignedUrlResponse generatePresignedUrl(String fileName, String fileType) {
        String uniqueFileName = generateUniqueFileName(fileName);
        Date expiration = Date.from(Instant.now().plusSeconds(600)); // 10 phút

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, uniqueFileName)
                .withMethod(HttpMethod.PUT)
                .withContentType(fileType)
                .withExpiration(expiration);

        return PresignedUrlResponse.builder()
                .presignedUrl(s3.generatePresignedUrl(request).toString())
                .imageUrl(getUrl(uniqueFileName))
                .build();
    }

    @Override
    @Transactional
    public ResourceResponse createResource(UploadResourceRequest request) {
        Resource resource = resourceRepository.save(resourceMapper.toResource(request));
        return resourceMapper.toResourceResponse(resource);
    }

    @Override
    @Transactional
    public ResourceResponse updateResource(Long resourceId, UploadResourceRequest request) {
        if (!resourceRepository.existsById(resourceId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        Resource resource = resourceMapper.toResource(request);
        resource.setResourceId(resourceId);
        return resourceMapper.toResourceResponse(resourceRepository.save(resource));
    }

    @Override
    @Transactional
    public void deleteResource(Long resourceId) {
        if (!resourceRepository.existsById(resourceId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        resourceRepository.deleteById(resourceId);
    }

    @Override
    public List<PresignedUrlResponse> getPresignedUrlsBatch(List<PresignedUrlRequest> requests) {
        return requests.stream()
                .map(req -> generatePresignedUrl(req.getFileName(), req.getContentType()))
                .collect(Collectors.toList());
    }

    @Override
    public Page<ResourceResponse> getAllResources(int page, int size, String sort, String direction) {
        Sort sortOrder = Sort.by(Sort.Direction.fromString(direction), sort);
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<Resource> resourceList = resourceRepository.findAll(pageable);
        List<String> userIds = resourceList.getContent().stream()
                .map(Resource::getUserId)
                .distinct()
                .toList();
        List<Long> subjectIds = resourceList.getContent().stream()
                .map(Resource::getSubjectId)
                .distinct()
                .toList();
        List<Long> schoolYearIds = resourceList.getContent().stream()
                .map(Resource::getSchoolYearId)
                .distinct()
                .toList();
        List<Long> gradeIds = resourceList.getContent().stream()
                .map(Resource::getGradeId)
                .distinct()
                .toList();
        Map<String, String> userMap = userClient.getUsersByIds(userIds).getResult().stream()
                .collect(Collectors.toMap(UserClient.UserResponse::getUserId, UserClient.UserResponse::getFullName));
        Map<Long, String> subjectMap = subjectClient.getSubjectByIds(subjectIds).getResult().stream()
                .collect(Collectors.toMap(subject -> Long.parseLong(subject.getId()), SubjectClient.SubjectResponse::getName));
        Map<Long, String> schoolYearMap = schoolClient.getSchoolYearsByIds(schoolYearIds).getResult().stream()
                .collect(Collectors.toMap(SchoolClient.SchoolYearResponse::getSchoolYearId, SchoolClient.SchoolYearResponse::getSchoolYear));
        Map<Long, String> gradeMap = schoolClient.getGradesByIds(gradeIds).getResult().stream()
                .collect(Collectors.toMap(SchoolClient.GradeResponse::getGradeId, SchoolClient.GradeResponse::getGradeName));
        return resourceList.map(resource -> {
            ResourceResponse response = resourceMapper.toResourceResponse(resource);
            response.setGradeName(gradeMap.get(resource.getGradeId()));
            response.setFullName(userMap.get(resource.getUserId()));
            response.setSubjectName(subjectMap.get(resource.getSubjectId()));
            response.setSchoolYear(schoolYearMap.get(resource.getSchoolYearId()));
            return response;
        });
    }

    public ResourceResponse getResourceById(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        String userFullName = userClient.getUserById(resource.getUserId())
                .getResult()
                .getFullName();

        String subjectName = subjectClient.getSubjectById(resource.getSubjectId())
                .getResult()
                .getName();

        String schoolYear = schoolClient.getSchoolYearById(resource.getSchoolYearId())
                .getResult()
                .getSchoolYear();

        String grade = schoolClient.getGradeById(resource.getGradeId())
                .getResult()
                .getGradeName();

        ResourceResponse response = resourceMapper.toResourceResponse(resource);
        response.setFullName(userFullName);
        response.setSubjectName(subjectName);
        response.setSchoolYear(schoolYear);
        response.setGradeName(grade);
        return response;
    }

    @Override
    public Page<ResourceResponse> searchResources(int page, int size, String sort, String direction, String keyword) {
        Sort sortOrder = Sort.by(Sort.Direction.fromString(direction), sort);
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<Resource> resourceList = resourceRepository.findAllByResourceNameContainingIgnoreCase(keyword, pageable);
        if (resourceList.isEmpty()) {
            return Page.empty(pageable);
        }
        List<String> userIds = resourceList.getContent().stream()
                .map(Resource::getUserId)
                .distinct()
                .toList();
        List<Long> subjectIds = resourceList.getContent().stream()
                .map(Resource::getSubjectId)
                .distinct()
                .toList();
        List<Long> schoolYearIds = resourceList.getContent().stream()
                .map(Resource::getSchoolYearId)
                .distinct()
                .toList();
        List<Long> gradeIds = resourceList.getContent().stream()
                .map(Resource::getGradeId)
                .distinct()
                .toList();

        Map<String, String> userMap = userClient.getUsersByIds(userIds).getResult().stream()
                .collect(Collectors.toMap(UserClient.UserResponse::getUserId, UserClient.UserResponse::getFullName));
        Map<Long, String> subjectMap = subjectClient.getSubjectByIds(subjectIds).getResult().stream()
                .collect(Collectors.toMap(subject -> Long.parseLong(subject.getId()), SubjectClient.SubjectResponse::getName));
        Map<Long, String> schoolYearMap = schoolClient.getSchoolYearsByIds(schoolYearIds).getResult().stream()
                .collect(Collectors.toMap(SchoolClient.SchoolYearResponse::getSchoolYearId, SchoolClient.SchoolYearResponse::getSchoolYear));
        Map<Long, String> gradeMap = schoolClient.getGradesByIds(gradeIds).getResult().stream()
                .collect(Collectors.toMap(SchoolClient.GradeResponse::getGradeId, SchoolClient.GradeResponse::getGradeName));
        return resourceList.map(resource -> {
            ResourceResponse response = resourceMapper.toResourceResponse(resource);
            response.setFullName(userMap.get(resource.getUserId()));
            response.setSubjectName(subjectMap.get(resource.getSubjectId()));
            response.setSchoolYear(schoolYearMap.get(resource.getSchoolYearId()));
            response.setGradeName(gradeMap.get(resource.getGradeId()));
            return response;
        });
    }

    @Override
    public Page<ResourceResponse> getAllResourcesBySubjectId(Long subjectId, Long gradeId, int page, int size, String sort, String direction) {
        Sort sortOrder = Sort.by(Sort.Direction.fromString(direction), sort);
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<Resource> resourceList = resourceRepository.findAllBySubjectIdAndGradeId(subjectId, gradeId, pageable);
        if (resourceList.isEmpty()) {
            return Page.empty(pageable);
        }
        List<String> userIds = resourceList.getContent().stream()
                .map(Resource::getUserId)
                .distinct()
                .toList();
        List<Long> schoolYearIds = resourceList.getContent().stream()
                .map(Resource::getSchoolYearId)
                .distinct()
                .toList();
        List<Long> gradeIds = resourceList.getContent().stream()
                .map(Resource::getGradeId)
                .distinct()
                .toList();

        Map<String, String> userMap = userClient.getUsersByIds(userIds).getResult().stream()
                .collect(Collectors.toMap(UserClient.UserResponse::getUserId, UserClient.UserResponse::getFullName));
        Map<Long, String> schoolYearMap = schoolClient.getSchoolYearsByIds(schoolYearIds).getResult().stream()
                .collect(Collectors.toMap(SchoolClient.SchoolYearResponse::getSchoolYearId, SchoolClient.SchoolYearResponse::getSchoolYear));
        Map<Long, String> gradeMap = schoolClient.getGradesByIds(gradeIds).getResult().stream()
                .collect(Collectors.toMap(SchoolClient.GradeResponse::getGradeId, SchoolClient.GradeResponse::getGradeName));

        return resourceList.map(resource -> {
            ResourceResponse response = resourceMapper.toResourceResponse(resource);
            response.setFullName(userMap.get(resource.getUserId()));
            response.setSchoolYear(schoolYearMap.get(resource.getSchoolYearId()));
            response.setGradeName(gradeMap.get(resource.getGradeId()));
            return response;
        });
    }

    @Override
    public List<PresignedUrlForAvatarResponse> getPresignedUrlsBatchForAvatarUpload(List<PresignedUrlForAvatarRequest> requests) {
        return requests.stream()
                .map(req -> {
                    PresignedUrlResponse presignedUrl = generatePresignedUrl(req.getFileName(), req.getContentType());
                    return new PresignedUrlForAvatarResponse(req.getRowIndex(), presignedUrl.getPresignedUrl(), presignedUrl.getImageUrl());
                })
                .collect(Collectors.toList());
    }

    @Async
    @Transactional
    public void upload(MultipartFile file, Long resourceId) {
        try (InputStream in = file.getInputStream()) {
            if (!resourceRepository.existsById(resourceId)) {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
            }

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            PutObjectRequest request = new PutObjectRequest(bucketName, resourceId+"", in, metadata);
            s3.putObject(request);

            resourceRepository.findById(resourceId).ifPresent(resource -> {
                resource.setUrl(getUrl(resourceId+""));
                resourceRepository.save(resource);
            });

        } catch (IOException e) {
            resourceRepository.deleteById(resourceId);
            throw new RuntimeException("File upload failed", e);
        }
    }

    public String getUrl(String objectName) {
        return s3.getUrl(bucketName, objectName).toString();
    }
}
