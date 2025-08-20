package com.datn.school_service.Services.UserService;

import com.datn.school_service.Dto.Respone.ApiResponse;
import com.datn.school_service.Dto.Respone.User.GetStudentInfo;
import com.datn.school_service.Dto.Respone.User.GetUserNameResponse;
import com.datn.school_service.Exceptions.AppException;
import com.datn.school_service.Exceptions.ErrorCode;
import com.datn.school_service.HttpClient.UserServiceClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetUserService {
    private final UserServiceClient userServiceClient;
    private final ObjectMapper objectMapper;


    public GetUserNameResponse getSingleUserInfo(String userId, String expectedRole) {

        ApiResponse<Map<String, Boolean>> roleCheck = userServiceClient.checkUserRole(
                Collections.singletonList(userId), expectedRole);

        if (roleCheck == null || roleCheck.getResult() == null ||
                !roleCheck.getResult().getOrDefault(userId, false)) {
            throw new AppException(ErrorCode.ENTITYS_NOT_FOUND,expectedRole,userId);
        }

        // Get user info
        ApiResponse<List<?>> userResponse = userServiceClient.getUsersByIds(Collections.singletonList(userId));
        if (userResponse == null || userResponse.getResult() == null || userResponse.getResult().isEmpty()) {
            throw new AppException(ErrorCode.ENTITYS_NOT_FOUND);
        }

        Object rawUser = userResponse.getResult().get(0);
        return objectMapper.convertValue(rawUser, GetUserNameResponse.class);
    }

    public List<GetUserNameResponse> getUsersInfo(List<String> userIds, String expectedRole) {
        if (userIds == null || userIds.isEmpty()) {
            throw new AppException(ErrorCode.INPUT_NULL, "userIds");
        }

        ApiResponse<Map<String, Boolean>> roleCheck = userServiceClient.checkUserRole(userIds, expectedRole);

        if (roleCheck == null || roleCheck.getResult() == null) {
            throw new AppException(ErrorCode.CALL_SERVICE_FALL, "Role check failed");
        }
        // Lọc ra các userId hợp lệ với role
        List<String> validIds = userIds.stream()
                .filter(id -> roleCheck.getResult().getOrDefault(id, false))
                .toList();

        if (validIds.isEmpty()) {
            throw new AppException(ErrorCode.ENTITYS_NOT_FOUND, expectedRole, userIds.toString());
        }

        ApiResponse<List<?>> userResponse = userServiceClient.getUsersByIds(validIds);
        if (userResponse == null || userResponse.getResult() == null) {
            throw new AppException(ErrorCode.CALL_SERVICE_FALL, "Failed to fetch users");
        }

        return userResponse.getResult().stream()
                .map(obj -> objectMapper.convertValue(obj, GetUserNameResponse.class))
                .toList();
    }

    public List<GetStudentInfo> getStudentInfo(List<String> userIds, String expectedRole, String className) {
        if (userIds == null || userIds.isEmpty()) {
            throw new AppException(ErrorCode.INPUT_NULL, "userIds");
        }

        ApiResponse<Map<String, Boolean>> roleCheck = userServiceClient.checkUserRole(userIds, expectedRole);

        if (roleCheck == null || roleCheck.getResult() == null || roleCheck.getResult().isEmpty()) {
            throw new AppException(ErrorCode.CALL_SERVICE_FALL, "Role check failed");
        }

        // Lọc ra các userId hợp lệ với role
        List<String> validIds = userIds.stream()
                .filter(id -> roleCheck.getResult().getOrDefault(id, false))
                .toList();

        if (validIds.isEmpty()) {
            throw new AppException(ErrorCode.ENTITYS_NOT_FOUND, expectedRole, userIds.toString());
        }

        ApiResponse<List<?>> userResponse = userServiceClient.getUsersByIds(validIds);
        if (userResponse == null || userResponse.getResult() == null || userResponse.getResult().isEmpty()) {
            throw new AppException(ErrorCode.CALL_SERVICE_FALL, "Failed to fetch users");
        }


        return userResponse.getResult().stream()
                .map(obj -> {
                    GetStudentInfo info = objectMapper.convertValue(obj, GetStudentInfo.class);
                    info.setClassName(className);
                    return info;
                })
                .toList();
    }
}
