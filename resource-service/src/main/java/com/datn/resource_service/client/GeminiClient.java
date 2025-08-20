package com.datn.resource_service.client;

import com.datn.resource_service.dto.gemini.GeminiRequest;
import com.datn.resource_service.dto.gemini.GeminiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "geminiClient", url = "https://generativelanguage.googleapis.com/v1beta")
public interface GeminiClient {

    @PostMapping("/models/gemini-2.0-flash:generateContent")
    GeminiResponse generateContent(
            @RequestParam("key") String apiKey,
            @RequestBody GeminiRequest request
    );
}