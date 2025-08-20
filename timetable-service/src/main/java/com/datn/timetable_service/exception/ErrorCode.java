package com.datn.timetable_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.OK),
    UNAUTHENTICATED(1001, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1002, "Unauthorized", HttpStatus.UNAUTHORIZED),
    INVALID_KEY(1003, "Invalid key", HttpStatus.BAD_REQUEST),
    INVALID_SLOT(1004, "Invalid timetable", HttpStatus.BAD_REQUEST),
    SLOT_CONFLICT(1005, "Slot conflict", HttpStatus.CONFLICT),
    SLOT_NOT_FOUND(1006, "Slot not found", HttpStatus.NOT_FOUND),
    SLOT_IS_NOT_DELETED(1007, "Slot is not deleted", HttpStatus.BAD_REQUEST),
    SEMESTER_ID_REQUIRED(1008, "Semester ID is required", HttpStatus.BAD_REQUEST),
    CREATE_SLOT_FAILED(1009, "Create slot failed", HttpStatus.INTERNAL_SERVER_ERROR),
    EXAM_INFO_NOT_FOUND(1010, "Exam info not found", HttpStatus.NOT_FOUND),
    EXAM_INFO_CONFLICT(1011, "Exam info conflict", HttpStatus.CONFLICT),
    EXAM_SESSION_NOT_FOUND(1012, "Exam session not found", HttpStatus.NOT_FOUND),
    EXAM_SESSION_CONFLICT(1013, "Exam session conflict", HttpStatus.CONFLICT),
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}