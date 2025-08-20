package com.datn.user_service.dto.response;

import com.datn.user_service.model.Student;
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
public class StudentResponse extends UserResponse {
    private Student.Status status;
    private List<ParentResponse> parents;
}
