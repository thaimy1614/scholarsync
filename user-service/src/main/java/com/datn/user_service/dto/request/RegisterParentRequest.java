package com.datn.user_service.dto.request;

import com.datn.user_service.model.ParentStudent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Register request for parents")
public class RegisterParentRequest extends RegisterUser {
    @Schema(description = "Student's email", example = "student@example.com")
    private String studentEmail;

    @Schema(description = "Enable notifications", example = "true")
    private Boolean isNotificationOn;

    @Schema(description = "Parent type", example = "MOTHER")
    private ParentStudent.ParentType parentType;

    public void setRole(String role) {
        super.setRole("parent");
    }
}
