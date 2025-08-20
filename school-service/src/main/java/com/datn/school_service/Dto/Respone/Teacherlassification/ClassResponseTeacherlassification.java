package com.datn.school_service.Dto.Respone.Teacherlassification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ClassResponseTeacherlassification {
    private Long classId;
    private String className;
}
