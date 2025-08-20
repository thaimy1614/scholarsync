package com.datn.user_service.dto.response;

import com.datn.user_service.model.ParentStudent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ParentResponse extends UserResponse {
    private Boolean isNotificationOn;
    private List<StudentResponse> students;
}
