package com.datn.user_service.dto.request;

import com.datn.user_service.model.Complaint;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckComplaintRequest {
    @Schema(description = "Status of complaint", example = "PENDING/IN_PROGRESS/RESOLVED")
    @NotEmpty(message = "Status is required")
    private Complaint.Status status;
}
