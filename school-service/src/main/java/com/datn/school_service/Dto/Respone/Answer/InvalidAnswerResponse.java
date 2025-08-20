package com.datn.school_service.Dto.Respone.Answer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor

public class InvalidAnswerResponse {
    @Builder.Default
    private List<Long> invalidAnswerId = null;
}
