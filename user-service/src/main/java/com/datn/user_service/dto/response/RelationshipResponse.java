package com.datn.user_service.dto.response;

import com.datn.user_service.model.ParentStudent;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RelationshipResponse {
    private ParentStudent.ParentType parentType;
    private ParentResponse parent;
    private StudentResponse student;
}
