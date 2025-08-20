package com.datn.school_service.Dto.Respone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsResponse {
    private Long newsId;

    private String newsTitle;

    private String newsContent;

    private String newsOwner;

    private String newsOwnerName;

    private LocalDateTime createdAt;

    private NewsTypeResponse newsTypeResponse;
}
