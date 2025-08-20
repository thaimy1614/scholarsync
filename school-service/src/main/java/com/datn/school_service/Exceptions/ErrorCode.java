    package com.datn.school_service.Exceptions;

    import lombok.Getter;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.HttpStatusCode;

    import java.text.MessageFormat;

    @Getter

    public enum ErrorCode {
        UNAUTHENTICATED(1111, "Unauthenticated", HttpStatus.OK),
        ENTITYS_NOT_FOUND(2001, "The requested find {0} by ID {1} was not found", HttpStatus.NOT_FOUND),
        INVALID_REQUEST(2002, "Invalid request for {0}", HttpStatus.BAD_REQUEST),
        FAILED_SAVE_ENTITY(2003, "Failed to save {0} entity", HttpStatus.INTERNAL_SERVER_ERROR),
        ENTITY_EMPTY(2004, "The request get {0} was empty", HttpStatus.NOT_FOUND),
        INPUT_NULL(2005, "{0} is null ", HttpStatus.BAD_REQUEST),
        ENTITY_ALREADY_EXIT(2006, "{0} already exit at {1}", HttpStatus.CONFLICT),
        ALL_ENTITY_EXITED(2007, "All student already study at this class {0}", HttpStatus.CONFLICT),
        CALL_SERVICE_FALL(2008, "Call service fall", HttpStatus.INTERNAL_SERVER_ERROR),
        INPUT_INVALID(2009, "Invalid input for {0}", HttpStatus.BAD_REQUEST),
        EMPTY_REQUEST(2012, "Empty request", HttpStatus.BAD_REQUEST),

        NEWS_TYPE_NOT_FOUND(2010,"Can't find new type because it was delete or not created", HttpStatus.NOT_FOUND),
        NEWS_TYPENAME_ALREADY_EXIT(2011,"This newstype name already exit", HttpStatus.CONFLICT),
        NEWS_TYPE_IS_DELETED(2013,"this news type is deleted", HttpStatus.CONFLICT),

        ROOM_NOT_FOUND(2014,"Can't find this room ", HttpStatus.NOT_FOUND),
        ROOM_NAME_ALREADY_EXIT(2015,"This room name already exit", HttpStatus.CONFLICT),
        ROOM_IS_DELETED(2016,"This room is deleted", HttpStatus.CONFLICT),

        ROOM_TYPE_NOT_FOUND(2017,"Can't find this roomtype because it was delete or not created", HttpStatus.NOT_FOUND),
        ROOM_TYPE_NAME_ALREADY_EXIT(2018,"This roomtype name already exit", HttpStatus.CONFLICT),
        ROOM_TYPE_IS_DELETED(2019,"This roomtype is deleted", HttpStatus.CONFLICT),

        CLASS_NOT_FOUND(2020,"Can't find this class", HttpStatus.NOT_FOUND),
        CLASS_NAME_ALREADY_EXIT(2021,"This class name already exit", HttpStatus.CONFLICT),
        CLASS_IS_DELETED(2022,"This class is deleted", HttpStatus.CONFLICT),

        SCHOOL_NOT_FOUND(2023,"Can't find this school", HttpStatus.NOT_FOUND),
        SCHOOL_NAME_ALREADY_EXIT(2024,"This school name already exit", HttpStatus.CONFLICT),
        SCHOOL_IS_DELETED(2025,"This school is deleted", HttpStatus.CONFLICT),

        GRADE_NOT_FOUND(2026, "Can't find this grade", HttpStatus.NOT_FOUND),
        GRADE_NAME_ALREADY_EXIT(2027, "This grade name already exit", HttpStatus.CONFLICT),
        GRADE_IS_DELETED(2028, "This grade is deleted", HttpStatus.CONFLICT),

        NEWS_NOT_FOUND(2029, "Can't find news because it was delete or not created", HttpStatus.NOT_FOUND),
        NEWS_CONTENT_ALREADY_EXIT(2030, "This news  content already exit", HttpStatus.CONFLICT),
        NEWS_IS_DELETED(2031, "this news is deleted", HttpStatus.CONFLICT),
        NEWS_TITLE_ALREADY_EXIT(2031, "This news title already exit", HttpStatus.CONFLICT),

        QUESTION_NOT_FOUND(2032,"Can't find this question", HttpStatus.NOT_FOUND),
        QUESTION_ALREADY_EXIT(2033,"This question already exit", HttpStatus.CONFLICT),
        QUESTION_IS_DELETED(2034,"This question is deleted", HttpStatus.CONFLICT),

        ANSWER_NOT_FOUND(2032, "Can't find this answer", HttpStatus.NOT_FOUND),
        ANSWER_ALREADY_EXIT(2033, "This answer already exit", HttpStatus.CONFLICT),
        ANSWER_IS_DELETED(2034, "This answer is deleted", HttpStatus.CONFLICT),

        TEACHER_NOT_EXIT(2035, "Can't find this teacher", HttpStatus.NOT_FOUND),
        STUDENT_NOT_EXIT(2036, "Can't find this student", HttpStatus.NOT_FOUND),

        SEMESTER_NOT_EXIT(2037, "Can't find this semester", HttpStatus.NOT_FOUND),
        SCHOOL_YEAR_NOT_EXIT(2038, "Can't find this year", HttpStatus.NOT_FOUND),
        ANSWER_NOT_MATCH_QUESTION(2039, "This answer not from this question", HttpStatus.CONFLICT),
        STUDENT_ALREADY_REPORT_TEACHER(2040,"This student just can report one time for one question with 1 teacher)", HttpStatus.CONFLICT),
        EVALUATION_SESSION_NOT_FOUND(2041,"Data invalid because don't have data of this report ", HttpStatus.NOT_FOUND),
        STUDENT_NOT_STUDY_IN_CLASS(2042,"student not study in class",HttpStatus.BAD_REQUEST),

        TEACHER_LASSIFICATION_IS_DELETED(2043,"Teacherlassification is already deleted", HttpStatus.CONFLICT),
        TEACHER_LASSIFICATION_NOT_EXITS(2044,"Teacherlassification is not exits", HttpStatus.CONFLICT),

        CLASS_AND_SUBJECT_ALREADY_EXITS(2045,"Class and subject already exits", HttpStatus.CONFLICT),
        SUBJECT_NOT_EXIT(2046,"Subject is not exits", HttpStatus.CONFLICT),
        CLASS_AND_SUBJECT_ALREADY_DELETE(2047,"Class and subject already delete", HttpStatus.CONFLICT),
        CLASS_SUBJECT_NOT_FOUND(2048,"Class subject not found", HttpStatus.NOT_FOUND),

        SUBJECT_NOT_FOUND(2049,"Subject is not found", HttpStatus.NOT_FOUND),
        TEACHER_NOT_TEACH_IN_CLASS(2050,"This teacher not teach in class",HttpStatus.BAD_REQUEST),

        VIOLATION_TYPE_NOT_FOUND(2032, "Can't find this ViolationType", HttpStatus.NOT_FOUND),
        VIOLATION_TYPE_ALREADY_EXIT(2033, "This ViolationType already exit", HttpStatus.CONFLICT),
        VIOLATION_TYPE_IS_DELETED(2034, "This ViolationType is deleted", HttpStatus.CONFLICT),

        VIOLATION_RECORD_NOT_FOUND(2032, "Can't find this ViolationRecord", HttpStatus.NOT_FOUND),
        VIOLATION_RECORD_ALREADY_EXIT(2033, "This ViolationRecord already exit", HttpStatus.CONFLICT),
        VIOLATION_RECORD_IS_DELETED(2034, "This ViolationRecord is deleted", HttpStatus.CONFLICT),

        SCHOOL_YEAR_NOT_FOUND(2035,"Can't find this school year", HttpStatus.NOT_FOUND),
        SCHOOL_YEAR_ALREADY_EXIT(2036,"This school year already exit", HttpStatus.CONFLICT),
        SCHOOL_YEAR_IS_DELETED(2037,"This school year is deleted", HttpStatus.CONFLICT),
        SCHOOL_YEAR_INVALID_DATE(2038, "End date must be after or equal to start date", HttpStatus.CONFLICT),


        SEMESTER_ALREADY_EXIT(2039,"This semester already exit in this school year", HttpStatus.CONFLICT),
        SEMESTER_IS_DELETED(2040,"This semester is deleted", HttpStatus.CONFLICT),
        SEMESTER_NOT_FOUND(2041,"This semester is not found", HttpStatus.CONFLICT),

        RECORD_VIOLATIONS_ALREADY_EXISTS(2042, "This record {0} violations already exists in this class today", HttpStatus.CONFLICT),
        RECORD_VIOLATIONS_IS_DELETED(2043, "This record {0} violations is deleted", HttpStatus.CONFLICT),
        RECORD_VIOLATIONS_NOT_FOUND(2044, "This record {0} violations is not found", HttpStatus.CONFLICT),
        RECORD_VIOLATIONS_OVERDUE(2045, "Overdue to edit violations", HttpStatus.OK),
        ENTITY_IS_ACTIVE(2046,"{0} is still active",HttpStatus.BAD_REQUEST),
        FILE_ERROR(2047,"Không thể đọc file Excel",HttpStatus.INTERNAL_SERVER_ERROR),
        DUPLICATE_ENTITY(2048,"{0} already exit at {1}",HttpStatus.CONFLICT),

        SCHOOL_YEAR_START_END_DATE_EXIT(2049,"Đã tồn tại start date hoặc end date chọn ngày khác",HttpStatus.CONFLICT),
        ;

        private final int code;
        private final String messageTemplate;
        private final HttpStatusCode statusCode;

        ErrorCode(int code, String messageTemplate, HttpStatusCode statusCode) {
            this.code = code;
            this.messageTemplate = messageTemplate;
            this.statusCode = statusCode;
        }

        public int getCode() {
            return code;
        }

        public String getMessage(Object... args) {
            return MessageFormat.format(messageTemplate, args);
        }

        public HttpStatusCode getStatusCode() {
            return statusCode;
        }
    }
