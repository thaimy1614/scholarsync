package com.datn.attendance_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.OK),
    UNAUTHENTICATED(1001, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1002, "Unauthorized", HttpStatus.UNAUTHORIZED),
    INVALID_KEY(1003, "Invalid key", HttpStatus.BAD_REQUEST),
    INVALID_SLOT(1004, "Invalid slot, must take attendance today", HttpStatus.BAD_REQUEST),
    TEACHER_NOT_FOUND(1005, "Teacher not found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(1006, "User not found", HttpStatus.NOT_FOUND),
    CLASS_NOT_FOUND(1007, "Class not found", HttpStatus.NOT_FOUND),
    INVALID_DATE_RECORD(1008, "Invalid date record", HttpStatus.BAD_REQUEST),
    ATTENDANCE_RECORD_NOT_FOUND(1009, "Attendance record not found", HttpStatus.NOT_FOUND),
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