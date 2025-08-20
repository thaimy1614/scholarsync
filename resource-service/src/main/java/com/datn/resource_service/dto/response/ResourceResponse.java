package com.datn.resource_service.dto.response;

import com.datn.resource_service.model.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResourceResponse {
    private Long resourceId;
    private String resourceName;
    private String userId;
    private String fullName;
    private Long schoolYearId;
    private String schoolYear;
    private Long subjectId;
    private String subjectName;
    private Long gradeId;
    private String gradeName;
    private String url;
    private String type;
    private Instant uploadedAt;
    private Long size;
}
