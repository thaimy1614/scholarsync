package com.datn.school_service.Dto.Request.News;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddNewsRequest {

    private String newsTitle;

    private String newsContent;

   private String newsOwner;

    private Long newsTypeId;
}
