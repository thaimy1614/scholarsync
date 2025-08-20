package com.datn.school_service.Controllers;
import com.datn.school_service.Dto.Request.News.AddNewsRequest;
import com.datn.school_service.Dto.Request.News.SearchNewsRequest;
import com.datn.school_service.Dto.Request.SearchNewsTypeRequest;
import com.datn.school_service.Dto.Respone.ApiResponse;
import com.datn.school_service.Dto.Respone.NewsResponse;
import com.datn.school_service.Dto.Respone.NewsTypeResponse;
import com.datn.school_service.Services.News.NewsServiceInterface;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${application.api.prefix}/news")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NewsController {
    
    private final NewsServiceInterface newsServiceInterface;

    @GetMapping("/getNewsById/{id}")
    public ApiResponse<NewsResponse> getNewsTypeById(@PathVariable Long id) {
        return ApiResponse.<NewsResponse>builder().result(newsServiceInterface.getNewsById(id)).build();
    }

    @PostMapping("/addNews")
    public ApiResponse<Void> createNewsType(@RequestBody AddNewsRequest addNewsRequest) {
        newsServiceInterface.createNews(addNewsRequest);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/editNews/{id}")
    public ApiResponse<Void> updateNewsType(
            @PathVariable Long id,
            @RequestBody @Valid AddNewsRequest addNewTypeRequest
    ) {
        newsServiceInterface.updateNews(id, addNewTypeRequest);
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping("/DeleteNews/{id}")
    public ApiResponse<Void> deleteNewsType(@PathVariable Long id) {
        newsServiceInterface.deleteNews(id);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/getAllNews/{active}")
    public ApiResponse<Page<NewsResponse>> getAllNewsTypesDelete(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "newsTitle") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            @PathVariable Boolean active

    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<NewsResponse>>builder()
                .result(newsServiceInterface.getAll(pageable, active))
                .build();
    }
    @PostMapping("/searchNews/{active}")
    public ApiResponse<List<NewsResponse>> searchNewsTypes(@RequestBody @Valid SearchNewsRequest keyword, @PathVariable boolean active) {
        return ApiResponse.<List<NewsResponse>>builder().result(newsServiceInterface.searchNews(keyword, active)).build();
    }
    @PutMapping("/activeNews/{id}")
    public ApiResponse<Void> activeNews(@PathVariable Long id) {
        newsServiceInterface.restoreNews(id);
        return ApiResponse.<Void>builder().build();
    }

}

