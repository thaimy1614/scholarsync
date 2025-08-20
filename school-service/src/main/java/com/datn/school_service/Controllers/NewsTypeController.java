package com.datn.school_service.Controllers;


import com.datn.school_service.Dto.Request.AddNewsTypeRequest;
import com.datn.school_service.Dto.Request.SearchNewsTypeRequest;
import com.datn.school_service.Dto.Request.UpdateNewTypeRequest;
import com.datn.school_service.Dto.Respone.ApiResponse;
import com.datn.school_service.Dto.Respone.NewsTypeResponse;
import com.datn.school_service.Services.newTypeService.NewsTypeServiceInterface;
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
@RequestMapping("${application.api.prefix}/news-types")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NewsTypeController {

    private final NewsTypeServiceInterface newsTypeService;

    @GetMapping("/getAllNewsTypeActive")
    public ApiResponse<Page<NewsTypeResponse>> getAllNewsTypesActive(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "newsTypeName") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction

    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<NewsTypeResponse>>builder()
                .result(newsTypeService.getAll(pageable, true))
                .build();
    }

    @GetMapping("/getAllNewsTypeDelete")
    public ApiResponse<Page<NewsTypeResponse>> getAllNewsTypesDelete(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "newsTypeName") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction

    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<NewsTypeResponse>>builder()
                .result(newsTypeService.getAll(pageable, false))
                .build();
    }

    @GetMapping("/getNewsTypeById/{id}")
    public ApiResponse<NewsTypeResponse> getNewsTypeById(@PathVariable Long id) {
        return ApiResponse.<NewsTypeResponse>builder().result(newsTypeService.getNewsTypeById(id)).build();
    }

    @PostMapping("/addNewsType")
    public ApiResponse<Void> createNewsType(@RequestBody @Valid AddNewsTypeRequest addNewsTypeRequest) {
        newsTypeService.createNewsType(addNewsTypeRequest);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/editNewsType/{id}")
    public ApiResponse<Void> updateNewsType(
            @PathVariable Long id,
            @RequestBody @Valid UpdateNewTypeRequest updateNewTypeRequest
    ) {
        newsTypeService.updateNewsType(id, updateNewTypeRequest);
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping("/DeleteNewsType/{id}")
    public ApiResponse<Void> deleteNewsType(@PathVariable Long id) {
        newsTypeService.deleteNewsType(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/searchNewsType/{active}")
    public ApiResponse<List<NewsTypeResponse>> searchNewsTypes(@RequestBody @Valid SearchNewsTypeRequest keyword, @PathVariable boolean active) {
        return ApiResponse.<List<NewsTypeResponse>>builder().result(newsTypeService.searchNewsType(keyword, active)).build();
    }
    @PutMapping("/activeNewsType/{id}")
    public ApiResponse<Void> activeNewsType(@PathVariable Long id) {
        newsTypeService.restoreNewsType(id);
        return ApiResponse.<Void>builder().build();
    }
}