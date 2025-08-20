package com.datn.user_service.service.complaint;

import com.datn.user_service.dto.request.ComplaintRequest;
import com.datn.user_service.dto.request.ResponseComplaintRequest;
import com.datn.user_service.dto.response.ComplaintResponse;
import org.springframework.data.domain.Page;

public interface ComplaintService {
    Page<ComplaintResponse> getAllComplaints(int page, int size, String sortBy, String direction);

    Page<ComplaintResponse> searchComplaint(int page, int size, String sortBy, String direction, String keyword);

    Page<ComplaintResponse> getMyComplaint(String myId, int page, int size, String sortBy, String direction);

    ComplaintResponse createComplaint(String myId, ComplaintRequest request);

    ComplaintResponse checkComplaint(Long complaintId, String status);

    ComplaintResponse responseComplaint(Long complaintId, ResponseComplaintRequest responseContent, String responderId);
}
