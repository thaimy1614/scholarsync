package com.datn.school_service.Controllers;

import com.datn.school_service.Dto.Request.Answer.AddAnswerRequest;
import com.datn.school_service.Dto.Request.Answer.SearchAnswerRequest;
import com.datn.school_service.Dto.Respone.ApiResponse;
import com.datn.school_service.Dto.Respone.Answer.AnswerResponse;
import com.datn.school_service.Dto.Respone.Question.InvalidQuestionResponse;
import com.datn.school_service.Services.Answer.AnswerServiceInterface;
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
@RequestMapping("${application.api.prefix}/answer")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AnswerController {
    private final AnswerServiceInterface answerService;

    @GetMapping("/getAllAnswerActive")
    public ApiResponse<Page<AnswerResponse>> getAllAnswersActive(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "answer") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction

    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<AnswerResponse>>builder()
                .result(answerService.getAll(pageable, true))
                .build();
    }

    @GetMapping("/getAllAnswerDelete")
    public ApiResponse<Page<AnswerResponse>> getAllAnswersDelete(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "answer") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction

    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<AnswerResponse>>builder()
                .result(answerService.getAll(pageable, false))
                .build();
    }

    @GetMapping("/getAnswerById/{id}")
    public ApiResponse<AnswerResponse> getAnswerById(@PathVariable Long id) {
        return ApiResponse.<AnswerResponse>builder().result(answerService.getAnswerById(id)).build();
    }

    @PostMapping("/addAnswer")
    public ApiResponse<Void> createAnswer(@RequestBody @Valid AddAnswerRequest addAnswerRequest) {
        answerService.createAnswer(addAnswerRequest);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/editAnswer/{id}")
    public ApiResponse<Void> updateAnswer(
            @PathVariable Long id,
            @RequestBody @Valid  AddAnswerRequest addAnswerRequest
    ) {
        answerService.updateAnswer(id, addAnswerRequest);
       return ApiResponse.<Void>builder().build();

    }

    @DeleteMapping("/DeleteAnswer/{id}")
    public ApiResponse<Void> deleteAnswer(@PathVariable Long id) {
        answerService.deleteAnswer(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/searchAnswer/{active}")
    public ApiResponse<List<AnswerResponse>> searchAnswers(@RequestBody @Valid SearchAnswerRequest keyword, @PathVariable boolean active) {
        return ApiResponse.<List<AnswerResponse>>builder().result(answerService.searchAnswer(keyword, active)).build();
    }
}
