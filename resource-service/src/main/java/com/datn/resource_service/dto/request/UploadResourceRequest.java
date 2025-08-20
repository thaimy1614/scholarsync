package com.datn.resource_service.dto.request;

import com.datn.resource_service.model.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadResourceRequest {
    @NotBlank(message = "Resource name cannot be empty")
    private String resourceName;

    @NotBlank(message = "User ID cannot be empty")
    private String userId;

    private Long schoolYearId;

    private Long gradeId;

    private Long subjectId;

    @NotBlank(message = "URL cannot be empty")
    @Pattern(regexp = "^(https?|ftp)://.*$", message = "Invalid URL format")
    private String url;

    @NotNull(message = "Size cannot be null")
    @Min(value = 1, message = "Size must be greater than 0")
    private Long size;

    @NotBlank(message = "Type cannot be empty")
    private String type;
}
