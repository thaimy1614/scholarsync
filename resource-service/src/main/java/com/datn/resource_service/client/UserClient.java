package com.datn.resource_service.client;

import com.datn.resource_service.dto.ApiResponse;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "userClient", url = "${client.user.url}/user")
public interface UserClient {
    @GetMapping("/by-ids")
    ApiResponse<List<UserResponse>> getUsersByIds(@RequestParam List<String> ids);

    @GetMapping("/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable String id);

    @Data
    static class UserResponse {
        private String userId;
        private String fullName;
    }
}
