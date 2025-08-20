package com.datn.user_service.controller;

import com.datn.user_service.dto.ApiResponse;
import com.datn.user_service.dto.request.*;
import com.datn.user_service.dto.response.*;
import com.datn.user_service.exception.AppException;
import com.datn.user_service.exception.ErrorCode;
import com.datn.user_service.service.account.AccountService;
import com.datn.user_service.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("${application.api.prefix}")
public class UserController {
    private final UserService userService;

    private final AccountService accountService;

    @PostMapping("/block")
    ApiResponse<String> blockUser(@RequestParam List<String> userIds) {
        userService.blockUsers(userIds);
        return ApiResponse.<String>builder().message("Block users successfully!").result("OK").build();
    }

    @PostMapping("/unblock")
    ApiResponse<String> unblockUser(@RequestParam List<String> userIds) {
        userService.unblockUsers(userIds);
        return ApiResponse.<String>builder().message("Unblock users successfully!").result("OK").build();
    }

    @GetMapping("/my-info")
    ApiResponse<Object> getMyInfo(JwtAuthenticationToken jwt) {
        String userId = jwt.getName();
        Object userResponse = userService.getMyInfo(userId);
        return ApiResponse.<Object>builder().message("Get info successfully!").result(userResponse).build();
    }

    @DeleteMapping("/delete")
    ApiResponse<String> deleteUser(@RequestParam List<String> userIds) {
        userService.deleteUsers(userIds);
        return ApiResponse.<String>builder().message("Delete users successfully!").result("OK").build();
    }

    @GetMapping("/{userId}")
    ApiResponse<Object> getUserInfoById(@PathVariable String userId) {
        var userResponse = userService.getMyInfo(userId);
        return ApiResponse.<Object>builder().message("Get user's info successfully!").result(userResponse).build();
    }

    @GetMapping("/teacher/{teacherId}")
    ApiResponse<TeacherResponse> getTeacherInfoById(@PathVariable String teacherId) {
        TeacherResponse teacherResponse = userService.getTeacherInfo(teacherId);
        return ApiResponse.<TeacherResponse>builder().message("Get teacher's info successfully!").result(teacherResponse).build();
    }

    @GetMapping("/teacher/bulk")
    ApiResponse<List<TeacherResponse>> getTeachersInfoById(@RequestParam List<String> ids) {
        List<TeacherResponse> teacherResponse = userService.getTeachersInfo(ids);
        return ApiResponse.<List<TeacherResponse>>builder().message("Get teacher's info successfully!").result(teacherResponse).build();
    }

    @GetMapping("/count")
    ApiResponse<CountUserResponse> countNumberOfUser() {
        CountUserResponse res = userService.countUsers();
        return ApiResponse.<CountUserResponse>builder().message("Get info successfully!").result(res).build();
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user profile", description = "Updates user profile based on the selected role. Select a role from the dropdown to view the corresponding schema (student, teacher, or parent). The request body schema changes based on the 'role' field.", responses = {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User updated successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")})
    ApiResponse<?> changeUserProfile(
            @PathVariable String userId,
            @RequestBody RegisterUser userRequest) {
        var response = userService.updateMyInfo(userId, userRequest);
        return ApiResponse.builder().result(response).build();
    }

    @GetMapping("/checkRoleUsers/{roleName}")
    public ApiResponse<Map<String, Boolean>> checkUserRole(@RequestParam List<String> userIds, @PathVariable String roleName) {
        Map<String, Boolean> check = accountService.checkUserRoles(userIds, roleName);

        return ApiResponse.<Map<String, Boolean>>builder().result(check).build();
    }

    @GetMapping("/search")
    ApiResponse<Page<?>> searchUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size, @RequestParam String key, @RequestParam(name = "role", required = false) String role) {
        Pageable pageable = PageRequest.of(page, size);
        if (key == null || key.isEmpty()) {
            return null;
        }

        Page<?> res = userService.searchUsers(key, role, pageable);

        return ApiResponse.<Page<?>>builder().result(res).build();
    }

    @GetMapping("/by-ids")
    public ApiResponse<List<?>> getUsersByIds(@RequestParam(value = "ids") List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new AppException(ErrorCode.IDS_NOT_EMPTY);
        }
        return ApiResponse.<List<?>>builder().message("Get users by IDs successfully").result(userService.getUsersByIds(ids)).build();
    }

    @GetMapping("/by-role")
    public ApiResponse<Page<?>> getUsersByRole(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size, @RequestParam(defaultValue = "userId") String sortBy, @RequestParam(defaultValue = "ASC") String direction, @RequestParam(value = "role", required = false) String role) {
        if (role == null || role.isEmpty()) {
            return ApiResponse.<Page<?>>builder().message("Role is required").result(userService.getAllUsers(page, size, sortBy, direction)).build();
        }

        return switch (role.toUpperCase()) {
            case "STUDENT" ->
                    ApiResponse.<Page<?>>builder().message("Role is required").result(userService.getAllStudents(page, size, sortBy, direction)).build();
            case "TEACHER" ->
                    ApiResponse.<Page<?>>builder().message("Role is required").result(userService.getAllTeachers(page, size, sortBy, direction)).build();
            case "PARENT" ->
                    ApiResponse.<Page<?>>builder().message("Role is required").result(userService.getAllParents(page, size, sortBy, direction)).build();
            case "PRINCIPAL" ->
                    ApiResponse.<Page<?>>builder().message("Role is required").result(userService.getAllPrincipals(page, size, sortBy, direction)).build();
            case "MONITOR" ->
                    ApiResponse.<Page<?>>builder().message("Role is required").result(userService.getAllMonitor(page, size, sortBy, direction)).build();
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        };
    }

    @GetMapping("/get-all-teachers")
    ApiResponse<List<TeacherResponse>> getAllTeachers(){
        List<TeacherResponse> teacherResponses = userService.getAllTeachers();
        return ApiResponse.<List<TeacherResponse>>builder()
                .message("Get all teachers successfully")
                .result(teacherResponses)
                .build();
    }

    @PostMapping("/set-disciple-monitor/{userId}")
    ApiResponse<StudentResponse> setDiscipleMonitor(@PathVariable String userId) {
        StudentResponse studentResponse = userService.setDiscipleMonitor(userId);
        return ApiResponse.<StudentResponse>builder().message("Set disciple monitor successfully!").
                result(studentResponse)
                .build();
    }

    @GetMapping("/student/{studentId}")
    ApiResponse<StudentResponse> getStudentInfo(@PathVariable String studentId) {
        StudentResponse studentResponse = userService.getStudentInfo(studentId);
        return ApiResponse.<StudentResponse>builder().message("Get student info successfully!").result(studentResponse).build();
    }

    @GetMapping("/get-user-ids-by-emails")
    public ApiResponse<List<Object>> getUserIdsByEmails(@RequestParam List<String> emails) {
        List<Object> result = accountService.findByEmails(emails);
        return ApiResponse.<List<Object>>builder()
                .message("Get list userId success")
                .result(result)
                .build();
    }





}
