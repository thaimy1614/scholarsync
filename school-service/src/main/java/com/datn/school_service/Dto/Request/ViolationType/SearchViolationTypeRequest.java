package com.datn.school_service.Dto.Request.ViolationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class SearchViolationTypeRequest {
   private String violationTypeKeyWordName;
}
