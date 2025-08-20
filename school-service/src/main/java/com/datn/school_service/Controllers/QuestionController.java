package com.datn.school_service.Controllers;

import com.datn.school_service.Dto.Request.Question.AddQuestionRequest;
import com.datn.school_service.Dto.Request.Question.SearchQuestionRequest;
import com.datn.school_service.Dto.Respone.Answer.InvalidAnswerResponse;
import com.datn.school_service.Dto.Respone.ApiResponse;
import com.datn.school_service.Dto.Respone.Question.QuestionResponse;
import com.datn.school_service.Services.Question.QuestionServiceInterface;
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
@RequestMapping("${application.api.prefix}/question")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QuestionController {
    private final QuestionServiceInterface questionService;

    @GetMapping("/getAllQuestionActive")
    public ApiResponse<Page<QuestionResponse>> getAllQuestionsActive(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "question") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction

    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<QuestionResponse>>builder()
                .result(questionService.getAll(pageable, true))
                .build();
    }

    @GetMapping("/getAllQuestionDelete")
    public ApiResponse<Page<QuestionResponse>> getAllQuestionsDelete(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "question") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction

    ) {
        Sort sortBy = Sort.by(sort);
        sortBy = direction.equals("asc") ? sortBy.ascending() : sortBy.descending();
        Pageable pageable = PageRequest.of(page, size, sortBy);
        return ApiResponse.<Page<QuestionResponse>>builder()
                .result(questionService.getAll(pageable, false))
                .build();
    }

    @GetMapping("/getQuestionById/{id}")
    public ApiResponse<QuestionResponse> getQuestionById(@PathVariable Long id) {
        return ApiResponse.<QuestionResponse>builder().result(questionService.getQuestionById(id)).build();
    }

    @PostMapping("/addQuestion")
    public ApiResponse<InvalidAnswerResponse> createQuestion(@RequestBody @Valid AddQuestionRequest addQuestionRequest) {
        return ApiResponse.<InvalidAnswerResponse>builder().result(questionService.createQuestion(addQuestionRequest)).build();
    }

    @PutMapping("/editQuestion/{id}")
    public ApiResponse<InvalidAnswerResponse> updateQuestion(
            @PathVariable Long id,
            @RequestBody @Valid  AddQuestionRequest addQuestionRequest
    ) {
        return ApiResponse.<InvalidAnswerResponse>builder().result(questionService.updateQuestion(id, addQuestionRequest)).build();
    }

    @DeleteMapping("/DeleteQuestion/{id}")
    public ApiResponse<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/searchQuestion/{active}")
    public ApiResponse<List<QuestionResponse>> searchQuestions(@RequestBody @Valid SearchQuestionRequest keyword, @PathVariable boolean active) {
        return ApiResponse.<List<QuestionResponse>>builder().result(questionService.searchQuestion(keyword, active)).build();
    } 
}
