package com.datn.user_service.controller;

import com.datn.user_service.dto.ApiResponse;
import com.datn.user_service.dto.request.UpdateRelationship;
import com.datn.user_service.dto.response.ParentResponse;
import com.datn.user_service.dto.response.RelationshipResponse;
import com.datn.user_service.dto.response.StudentResponse;
import com.datn.user_service.model.ParentStudent;
import com.datn.user_service.service.parentStudent.ParentStudentService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.shaded.com.google.protobuf.Api;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${application.api.prefix}/parent-student")
@RequiredArgsConstructor
public class ParentStudentController {

    private final ParentStudentService parentStudentService;

    @GetMapping("/parents/{parentId}/children")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getAllChildrenOfParent(@PathVariable String parentId) {
        List<StudentResponse> children = parentStudentService.getAllChildrenOfParent(parentId);
        return ResponseEntity.ok(ApiResponse.<List<StudentResponse>>builder()
                .result(children)
                .build());
    }

    @GetMapping("/students/{studentId}/parents")
    public ResponseEntity<ApiResponse<List<ParentResponse>>> getAllParentsOfChild(@PathVariable String studentId) {
        List<ParentResponse> parents = parentStudentService.getAllParentsOfChild(studentId);
        return ResponseEntity.ok(ApiResponse.<List<ParentResponse>>builder()
                .result(parents)
                .build());
    }

    @PostMapping("/parents/{parentId}/students/{studentId}")
    public ResponseEntity<ApiResponse<String>> addParentStudentRelation(
            @PathVariable String parentId,
            @PathVariable String studentId,
            @RequestBody UpdateRelationship request) {
        parentStudentService.addParentStudentRelation(parentId, studentId, request.getParentType());
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .result("Parent-Student relation added successfully")
                .build());
    }

    @DeleteMapping("/parents/{parentId}/students/{studentId}")
    public ResponseEntity<ApiResponse<String>> removeParentStudentRelation(
            @PathVariable String parentId,
            @PathVariable String studentId) {
        parentStudentService.removeParentStudentRelation(parentId, studentId);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .result("Parent-Student relation removed successfully")
                .build());
    }

    @PutMapping("/parents/{parentId}/students/{studentId}/parent-type")
    public ResponseEntity<ApiResponse<String>> updateParentType(
            @PathVariable String parentId,
            @PathVariable String studentId,
            @RequestBody UpdateRelationship request) {
        parentStudentService.updateParentType(parentId, studentId, request.getParentType());
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .result("Parent type updated successfully")
                .build());
    }

    @GetMapping("/students/{studentId}/parents/by-type")
    public ResponseEntity<ApiResponse<List<ParentResponse>>> getParentsByTypeForChild(
            @PathVariable String studentId,
            @RequestParam ParentStudent.ParentType type) {
        List<ParentResponse> parents = parentStudentService.getParentsByTypeForChild(studentId, type);
        return ResponseEntity.ok(ApiResponse.<List<ParentResponse>>builder()
                .result(parents)
                .build());
    }

    @GetMapping("/parents/{parentId}/students/{studentId}/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkParentStudentRelation(
            @PathVariable String parentId,
            @PathVariable String studentId) {
        boolean exists = parentStudentService.checkParentStudentRelation(parentId, studentId);
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .result(exists)
                .build());
    }

    @GetMapping("/parents/{parentId}/children/by-type")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getChildrenByParentType(
            @PathVariable String parentId,
            @RequestParam ParentStudent.ParentType type) {
        List<StudentResponse> children = parentStudentService.getChildrenByParentType(parentId, type);
        return ResponseEntity.ok(ApiResponse.<List<StudentResponse>>builder()
                .result(children)
                .build());
    }

    @PostMapping("/students/{studentId}/notify-parents")
    public ResponseEntity<ApiResponse<String>> notifyParentsOfChild(
            @PathVariable String studentId,
            @RequestBody String message) {
        parentStudentService.notifyParentsOfChild(studentId, message);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .result("Notifications sent successfully")
                .build());
    }

    @GetMapping("/get-relationship")
    ApiResponse<RelationshipResponse> getRelationship(
            @RequestParam String parentId,
            @RequestParam String studentId) {
        RelationshipResponse relationship = parentStudentService.getRelationship(parentId, studentId);
        return ApiResponse.<RelationshipResponse>builder()
                .result(relationship)
                .build();
    }
}