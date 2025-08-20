package com.datn.user_service.dto.request;

import com.datn.user_service.model.ParentStudent;
import lombok.Data;

@Data
public class UpdateRelationship {
    private ParentStudent.ParentType parentType;
}
