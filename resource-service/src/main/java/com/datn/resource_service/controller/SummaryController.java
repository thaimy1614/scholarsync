package com.datn.resource_service.controller;

import com.datn.resource_service.dto.ApiResponse;
import com.datn.resource_service.dto.gemini.QuestionAnswerSet;
import com.datn.resource_service.service.FileSummaryService;
import com.datn.resource_service.util.FileUtil;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("${application.api.prefix}/summarize")
@RequiredArgsConstructor
public class SummaryController {
    private final FileSummaryService fileSummaryService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<QuestionAnswerSet> summarizeFile(
            @Parameter(description = "Upload file", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
            @RequestParam("file") MultipartFile multipartFile) {
        try {
            File tempFile = File.createTempFile("upload-", multipartFile.getOriginalFilename());
            multipartFile.transferTo(tempFile);

            String content = FileUtil.readFileContent(tempFile);

            QuestionAnswerSet result = fileSummaryService.generateSummaryAndQuestions(content);

            tempFile.delete();

            return ApiResponse.<QuestionAnswerSet>builder()
                    .result(result)
                    .message("Tóm tắt thành công")
                    .build();


        } catch (Exception e) {
            return ApiResponse.<QuestionAnswerSet>builder()
                    .result(null)
                    .message("Lỗi khi tóm tắt: " + e.getMessage())
                    .build();
        }
    }
}
