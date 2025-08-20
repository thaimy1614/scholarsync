package com.datn.user_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComplaintRequest {
    @Schema(description = "Content of complaint", example = "Truong nhu c")
    @NotEmpty(message = "Content is required")
    private String content;
}
