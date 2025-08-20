package com.datn.user_service.httpclient;

import com.datn.user_service.dto.ApiResponse;
import com.datn.user_service.dto.client.PresignedUrlRequest;
import com.datn.user_service.dto.client.PresignedUrlResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "resource-service-client", url = "${application.resource-service-url}")
public interface ResourceServiceClient {
    @PostMapping(value = "/resource/presigned-urls/avatar")
    ApiResponse<List<PresignedUrlResponse>> getPresignedUrls(@RequestBody List<PresignedUrlRequest> requests);
}