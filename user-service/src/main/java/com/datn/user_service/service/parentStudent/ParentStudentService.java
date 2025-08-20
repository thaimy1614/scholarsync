package com.datn.user_service.service.parentStudent;

import com.datn.user_service.dto.response.ParentResponse;
import com.datn.user_service.dto.response.RelationshipResponse;
import com.datn.user_service.dto.response.StudentResponse;
import com.datn.user_service.model.ParentStudent;

import java.util.List;

public interface ParentStudentService {
    List<StudentResponse> getAllChildrenOfParent(String parentId);

    List<ParentResponse> getAllParentsOfChild(String studentId);

    void notifyParentsOfChild(String studentId, String message);

    List<StudentResponse> getChildrenByParentType(String parentId, ParentStudent.ParentType type);

    boolean checkParentStudentRelation(String parentId, String studentId);

    List<ParentResponse> getParentsByTypeForChild(String studentId, ParentStudent.ParentType type);

    void updateParentType(String parentId, String studentId, ParentStudent.ParentType parentType);

    void addParentStudentRelation(String parentId, String studentId, ParentStudent.ParentType parentType);

    void removeParentStudentRelation(String parentId, String studentId);

    RelationshipResponse getRelationship(String parentId, String studentId);
}
