package com.datn.school_service.Services.News;

import com.datn.school_service.Dto.Request.News.AddNewsRequest;
import com.datn.school_service.Dto.Request.News.SearchNewsRequest;
import com.datn.school_service.Dto.Respone.NewsResponse;
import com.datn.school_service.Dto.Respone.NewsTypeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NewsServiceInterface {

    Page<NewsResponse> getAll(Pageable pageable, boolean active);

    NewsResponse getNewsById(Long id);

    void createNews(AddNewsRequest addNewTypeRequest);

    void updateNews(Long id, AddNewsRequest addNewsRequest);

    void deleteNews(Long id);

    void restoreNews(Long id);

    List<NewsResponse> searchNews(SearchNewsRequest keyword, boolean active);


}
