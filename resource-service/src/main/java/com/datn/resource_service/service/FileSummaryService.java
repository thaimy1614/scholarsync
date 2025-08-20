package com.datn.resource_service.service;

import com.datn.resource_service.client.GeminiClient;
import com.datn.resource_service.dto.gemini.GeminiRequest;
import com.datn.resource_service.dto.gemini.GeminiResponse;
import com.datn.resource_service.dto.gemini.QuestionAnswerSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileSummaryService {

    private final GeminiClient geminiClient;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public QuestionAnswerSet generateSummaryAndQuestions(String content) {
        content = content.length() > 12000 ? content.substring(0, 12000) : content;

        String prompt = """
    Tóm tắt đoạn văn sau, sau đó tạo ra 10 câu hỏi trắc nghiệm xoay quanh nội dung chính. Ngôn ngữ trả về (cả tóm tắt và câu hỏi) tùy thuộc vào ngôn ngữ của đoạn văn.
    
    Yêu cầu cho câu hỏi:
    - Mỗi câu có 4 đáp án
    - Chỉ 1 đáp án đúng
    - Các đáp án còn lại phải liên quan nhưng sai
    - Trả về ở dạng JSON với định dạng sau:

    {
      "summary": "Đây là đoạn tóm tắt...",
      "questions": [
        {
          "question": "Câu hỏi 1?",
          "options": ["A. nội dung đáp án", "B. nội dung đáp án", "C. nội dung đáp án", "D. nội dung đáp án"],
          "answer": "A/B/C/D"
        }
      ]
    }

    Nội dung cần xử lý:
    """ + content;

        GeminiRequest request = new GeminiRequest(
                List.of(new GeminiRequest.Content(
                        List.of(new GeminiRequest.Part(prompt))
                ))
        );

        GeminiResponse response = geminiClient.generateContent(geminiApiKey, request);
        String rawText = response.getCandidates().get(0).getContent().getParts().get(0).getText();

        String cleaned = cleanJsonMarkdown(rawText);
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(cleaned, QuestionAnswerSet.class);
        } catch (Exception e) {
            throw new RuntimeException("Không thể phân tích kết quả từ Gemini: " + e.getMessage() + "\nRaw: " + cleaned);
        }
    }

    private String cleanJsonMarkdown(String rawText) {
        if (rawText.startsWith("```")) {
            int start = rawText.indexOf("{");
            int end = rawText.lastIndexOf("}");
            if (start >= 0 && end >= 0 && end > start) {
                return rawText.substring(start, end + 1);
            }
        }
        return rawText;
    }

}

