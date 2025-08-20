package com.datn.user_service.service.complaint;

import com.datn.user_service.dto.request.ComplaintRequest;
import com.datn.user_service.dto.request.ResponseComplaintRequest;
import com.datn.user_service.dto.response.ComplaintResponse;
import com.datn.user_service.exception.AppException;
import com.datn.user_service.exception.ErrorCode;
import com.datn.user_service.model.Complaint;
import com.datn.user_service.model.User;
import com.datn.user_service.repository.ComplaintRepository;
import com.datn.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {
    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;

    public Page<ComplaintResponse> getAllComplaints(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Complaint> complaintPage = complaintRepository.findAll(pageable);

        return getComplaintResponses(complaintPage);
    }

    public Page<ComplaintResponse> getComplaintResponses(Page<Complaint> complaintPage) {
        List<String> listOfSenderIds = complaintPage.getContent().stream()
                .map(Complaint::getSenderId)
                .distinct()
                .toList();

        List<String> listOfResponderIds = complaintPage.getContent().stream()
                .map(Complaint::getResponderId)
                .distinct()
                .toList();

        List<User> listOfSender = userRepository.findAllById(listOfSenderIds);
        List<User> listOfResponder = userRepository.findAllById(listOfResponderIds);

        Map<String, User> senderMap = listOfSender.stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));
        Map<String, User> responderMap = listOfResponder.stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        return complaintPage.map(complaint -> {
            User sender = senderMap.get(complaint.getSenderId());
            User responder = responderMap.get(complaint.getResponderId());

            return ComplaintResponse.builder()
                    .complaintId(complaint.getComplaintId())
                    .senderId(complaint.getSenderId())
                    .senderName(sender.getFullName())
                    .responderId(complaint.getResponderId() != null ? complaint.getResponderId() : "")
                    .responderName(responder != null ? responder.getFullName() : "")
                    .content(complaint.getContent())
                    .status(complaint.getStatus())
                    .responseContent(complaint.getResponseContent())
                    .responseDate(complaint.getResponseDate())
                    .createdAt(complaint.getCreatedAt())
                    .build();
        });
    }

    public Page<ComplaintResponse> searchComplaint(int page, int size, String sortBy, String direction, String keyword) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Complaint> complaintPage = complaintRepository.findByContentContainingOrResponseContentContainingOrSenderIdContaining(keyword, keyword, keyword, pageable);

        return getComplaintResponses(complaintPage);
    }

    public Page<ComplaintResponse> getMyComplaint(String myId, int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Complaint> complaintPage = complaintRepository.findAllBySenderId(myId, pageable);

        List<String> listOfResponderIds = complaintPage.getContent().stream()
                .map(Complaint::getResponderId)
                .distinct()
                .toList();

        List<User> listOfAboutTeacher = userRepository.findAllById(listOfResponderIds);
        Map<String, User> responderMap = listOfAboutTeacher.stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        User sender = userRepository.findById(myId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return complaintPage.map(complaint -> {
            User responder = responderMap.get(complaint.getResponderId());
            return ComplaintResponse.builder()
                    .complaintId(complaint.getComplaintId())
                    .senderId(complaint.getSenderId())
                    .senderName(sender.getFullName())
                    .responderId(complaint.getResponderId() != null ? complaint.getResponderId() : "")
                    .responderName(responder != null ? responder.getFullName() : "")
                    .content(complaint.getContent())
                    .status(complaint.getStatus())
                    .responseContent(complaint.getResponseContent())
                    .responseDate(complaint.getResponseDate())
                    .createdAt(complaint.getCreatedAt())
                    .build();
        });
    }

    public ComplaintResponse createComplaint(String myId, ComplaintRequest request) {
        Complaint complaint = Complaint.builder()
                .senderId(myId)
                .content(request.getContent())
                .status(Complaint.Status.PENDING)
                .build();

        return getComplaintResponse(complaint);
    }

    public ComplaintResponse checkComplaint(Long complaintId, String status) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new AppException(ErrorCode.COMPLAINT_NOT_EXISTED));

        switch (status.toUpperCase()) {
            case "PENDING" -> complaint.setStatus(Complaint.Status.PENDING);
            case "IN_PROGRESS" -> complaint.setStatus(Complaint.Status.IN_PROGRESS);
            case "RESOLVED" -> complaint.setStatus(Complaint.Status.RESOLVED);
            default -> throw new AppException(ErrorCode.COMPLAINT_STATUS_NOT_EXISTED);
        }

        return getComplaintResponse(complaint);
    }

    public ComplaintResponse responseComplaint(Long complaintId, ResponseComplaintRequest request, String responderId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new AppException(ErrorCode.COMPLAINT_NOT_EXISTED));

        complaint.setResponseContent(request.getResponseContent());
        complaint.setResponderId(responderId);
        complaint.setResponseDate(LocalDateTime.now());

        return getComplaintResponse(complaint);
    }

    public ComplaintResponse getComplaintResponse(Complaint complaint) {
        complaint = complaintRepository.save(complaint);

        return ComplaintResponse.builder()
                .complaintId(complaint.getComplaintId())
                .senderId(complaint.getSenderId()==null ? "" : complaint.getSenderId())
                .senderName(userRepository.findById(complaint.getSenderId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)).getFullName())
                .responderId(complaint.getResponderId()==null ? "" : complaint.getResponderId())
                .responderName(complaint.getResponderId()==null ? "" : userRepository.findById(complaint.getResponderId())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)).getFullName())
                .content(complaint.getContent())
                .createdAt(complaint.getCreatedAt())
                .responseContent(complaint.getResponseContent())
                .responseDate(complaint.getResponseDate())
                .status(complaint.getStatus())
                .build();
    }
}
