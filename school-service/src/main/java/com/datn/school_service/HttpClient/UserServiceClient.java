package com.datn.school_service.HttpClient;


import com.datn.school_service.Dto.Respone.ApiResponse;

import com.datn.school_service.Dto.Respone.User.GetUserNameResponse;
import com.datn.school_service.Dto.Respone.User.UserIdResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;

@FeignClient(name = "user-service-client", url = "${application.user-service}/user")
public interface UserServiceClient {
    @GetMapping("/checkRoleUsers/{roleName}")
    ApiResponse<Map<String, Boolean>> checkUserRole(@RequestParam List<String> userIds, @PathVariable String roleName);

    @GetMapping("/by-ids")
        // lay thong tin user theo id
    ApiResponse<List<?>> getUsersByIds(@RequestParam List<String> ids);

    @GetMapping("/get-user-ids-by-emails")
    ApiResponse<List<Object>> getUserIdsByEmails(@RequestParam List<String> emails);
}
