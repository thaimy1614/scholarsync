package com.datn.user_service.dto.response;

import com.datn.user_service.model.Complaint;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ComplaintResponse {
    @Id
    private Long complaintId;
    private String senderId;
    private String senderName;
    private String responderId;
    private String responderName;
    private String content;
    private Complaint.Status status;
    private String responseContent;
    private LocalDateTime responseDate;
    private LocalDateTime createdAt;
}
