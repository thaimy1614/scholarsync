package com.datn.school_service.Services.newTypeService;

import com.datn.school_service.Dto.Request.AddNewsTypeRequest;
import com.datn.school_service.Dto.Request.SearchNewsTypeRequest;
import com.datn.school_service.Dto.Request.UpdateNewTypeRequest;
import com.datn.school_service.Dto.Respone.NewsTypeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NewsTypeServiceInterface {
    Page<NewsTypeResponse> getAll(Pageable pageable, boolean active);

    NewsTypeResponse getNewsTypeById(Long id);

    void createNewsType(AddNewsTypeRequest addNewTypeRequest);

    void updateNewsType(Long id, UpdateNewTypeRequest updateNewTypeRequest);

    void deleteNewsType(Long id);

    void restoreNewsType(Long id);

    List<NewsTypeResponse> searchNewsType(SearchNewsTypeRequest keyword, boolean active);
}
