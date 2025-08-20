package com.datn.attendance_service.service;

import com.datn.attendance_service.client.SchoolClient;
import com.datn.attendance_service.client.SubjectClient;
import com.datn.attendance_service.client.TimetableClient;
import com.datn.attendance_service.client.UserClient;
import com.datn.attendance_service.dto.ClassResponse;
import com.datn.attendance_service.dto.RoomResponse;
import com.datn.attendance_service.dto.kafka.AttendanceReminderToParent;
import com.datn.attendance_service.dto.kafka.WarningReminderToParent;
import com.datn.attendance_service.dto.request.RecordStudentAttendanceRequest;
import com.datn.attendance_service.dto.response.AttendanceSummaryResponse;
import com.datn.attendance_service.dto.response.RecordStudentAttendanceResponse;
import com.datn.attendance_service.dto.response.TeacherAttendanceHistoryResponse;
import com.datn.attendance_service.dto.subject_service.SubjectResponse;
import com.datn.attendance_service.dto.timetable_service.SlotDetailResponse;
import com.datn.attendance_service.dto.user_service.ParentResponse;
import com.datn.attendance_service.dto.user_service.StudentResponse;
import com.datn.attendance_service.dto.user_service.TeacherResponse;
import com.datn.attendance_service.exception.AppException;
import com.datn.attendance_service.exception.ErrorCode;
import com.datn.attendance_service.model.*;
import com.datn.attendance_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {
    private final AttendanceRecordRepository attendanceRecordRepository;

    private final TeacherAttendanceRepository teacherAttendanceRepository;

    private final NotificationRepository notificationRepository;

    private final AttendanceSummaryRepository attendanceSummaryRepository;

    private final AttendanceConfigRepository attendanceConfigRepository;

    private final UserClient studentClient;

    private final TimetableClient timetableClient;
    private final SchoolClient schoolClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SubjectClient subjectClient;

    private Map<String, String> getConfig() {
        return attendanceConfigRepository.findAll().stream()
                .collect(Collectors.toMap(AttendanceConfig::getConfigKey, AttendanceConfig::getConfigValue));
    }

    @Transactional
    public RecordStudentAttendanceResponse recordStudentAttendance(RecordStudentAttendanceRequest request) {
        // Validate timetable
        SlotDetailResponse timetable = timetableClient.getTimetable(request.getTimetableId()).getResult();
        if (!timetable.getDate().equals(request.getAttendanceDate())) {
            throw new AppException(ErrorCode.INVALID_SLOT);
        }

        if (!LocalDate.now().isEqual(timetable.getDate())) {
            throw new AppException(ErrorCode.INVALID_DATE_RECORD);
        }

        // Calculate score
        Map<String, String> config = getConfig();
        double score = Double.parseDouble(config.get("PRESENT_SCORE"));
        switch (request.getStatus()) {
            case ABSENT:
                score = Double.parseDouble(config.get("ABSENT_SCORE"));
                break;
            case LATE:
                score -= Double.parseDouble(config.get("LATE_DEDUCTION"));
                break;
            case EARLY_LEAVE:
                score -= Double.parseDouble(config.get("EARLY_LEAVE_DEDUCTION"));
                break;
        }

        AttendanceRecord record = new AttendanceRecord();

        // Check if record already exists
        if (attendanceRecordRepository.existsByStudentIdAndTimetableId(request.getStudentId(), request.getTimetableId())) {
            record = attendanceRecordRepository.findByStudentIdAndTimetableId(request.getStudentId(), request.getTimetableId())
                    .orElseThrow(() -> new AppException(ErrorCode.ATTENDANCE_RECORD_NOT_FOUND));
            record.setStatus(request.getStatus());
            record.setAttendanceScore(score);
            record.setUpdatedAt(LocalDateTime.now());
            record.setAttendanceDate(request.getAttendanceDate());
        } else {
            record = AttendanceRecord.builder()
                    .studentId(request.getStudentId())
                    .classId(timetable.getClassId())
                    .subjectId(timetable.getSubjectId())
                    .teacherId(timetable.getTeacherId())
                    .attendanceDate(request.getAttendanceDate())
                    .session(timetable.getMainSession())
                    .status(request.getStatus())
                    .attendanceScore(score)
                    .timetableId(request.getTimetableId())
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        AttendanceRecord savedRecord = attendanceRecordRepository.save(record);

        log.info("Attendance record saved: {}", record);
        // Send notification
        if (record.getStatus() != AttendanceRecord.AttendanceStatus.PRESENT) {
            log.info("Sending notification for attendance record: {}", savedRecord);
            sendNotification(savedRecord);
        }

        return mapToRecordResponse(savedRecord);
    }

    @Transactional
    public RecordStudentAttendanceResponse recordStudentAttendanceForAdmin(RecordStudentAttendanceRequest request) {
        // Validate timetable
        SlotDetailResponse timetable = timetableClient.getTimetable(request.getTimetableId()).getResult();

        // Calculate score
        Map<String, String> config = getConfig();
        double score = Double.parseDouble(config.get("PRESENT_SCORE"));
        switch (request.getStatus()) {
            case ABSENT:
                score = Double.parseDouble(config.get("ABSENT_SCORE"));
                break;
            case LATE:
                score -= Double.parseDouble(config.get("LATE_DEDUCTION"));
                break;
            case EARLY_LEAVE:
                score -= Double.parseDouble(config.get("EARLY_LEAVE_DEDUCTION"));
                break;
        }

        AttendanceRecord record = new AttendanceRecord();

        if (attendanceRecordRepository.existsByStudentIdAndTimetableId(request.getStudentId(), request.getTimetableId())) {
            record = attendanceRecordRepository.findByStudentIdAndTimetableId(request.getStudentId(), request.getTimetableId())
                    .orElseThrow(() -> new AppException(ErrorCode.ATTENDANCE_RECORD_NOT_FOUND));
            record.setStatus(request.getStatus());
            record.setAttendanceScore(score);
            record.setUpdatedAt(LocalDateTime.now());
            record.setAttendanceDate(request.getAttendanceDate());
        } else {
            record = AttendanceRecord.builder()
                    .studentId(request.getStudentId())
                    .classId(timetable.getClassId())
                    .subjectId(timetable.getSubjectId())
                    .teacherId(timetable.getTeacherId())
                    .attendanceDate(request.getAttendanceDate())
                    .session(timetable.getMainSession())
                    .status(request.getStatus())
                    .attendanceScore(score)
                    .timetableId(request.getTimetableId())
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        AttendanceRecord savedRecord = attendanceRecordRepository.save(record);

        // Send notification
        if (record.getStatus() != AttendanceRecord.AttendanceStatus.PRESENT) {
            sendNotification(savedRecord);
        }

        return mapToRecordResponse(savedRecord);
    }

    public RecordStudentAttendanceResponse mapToRecordResponse(AttendanceRecord record) {
        return RecordStudentAttendanceResponse.builder()
                .id(record.getId())
                .attendanceDate(record.getAttendanceDate())
                .studentId(record.getStudentId())
                .timetableId(record.getTimetableId())
                .classId(record.getClassId())
                .subjectId(record.getSubjectId())
                .teacherId(record.getTeacherId())
                .session(record.getSession())
                .status(record.getStatus())
                .attendanceScore(record.getAttendanceScore())
                .build();
    }

    @Transactional
    public List<RecordStudentAttendanceResponse> recordBulkAttendance(List<RecordStudentAttendanceRequest> records) {
        return records.stream().map(this::recordStudentAttendance).collect(Collectors.toList());
    }

    @Transactional
    public RecordStudentAttendanceResponse correctAttendance(Long id, AttendanceRecord.AttendanceStatus newStatus, Long updatedBy) {
        AttendanceRecord record = attendanceRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attendance record not found"));
        record.setStatus(newStatus);
        record.setUpdatedAt(LocalDateTime.now());
        // Log audit (assumed external audit service)
        RecordStudentAttendanceRequest request = RecordStudentAttendanceRequest.builder()
                .timetableId(record.getTimetableId())
                .status(newStatus)
                .attendanceDate(record.getAttendanceDate())
                .studentId(record.getStudentId())
                .build();
        return recordStudentAttendance(request);
    }

    @Transactional
    public TeacherAttendance recordTeacherAttendance(TeacherAttendance record) {
        return teacherAttendanceRepository.save(record);
    }

    private void sendNotification(AttendanceRecord record) {
        StudentResponse student = studentClient.getStudent(record.getStudentId()).getResult();
        if (student == null || student.getParents() == null || student.getParents().isEmpty()) {
            return; // No parent to notify
        }
        student.getParents().forEach(parent -> {
            if (parent.getIsNotificationOn()) {
                sendNotificationToParent(record, parent, student.getFullName());
            }
        });
    }

    private void sendNotificationToParent(AttendanceRecord record, ParentResponse parent, String studentName) {
        Notification notification = new Notification();
        notification.setStudentId(record.getStudentId());
        notification.setParentEmail(parent.getEmail());
        notification.setAttendanceRecordId(record.getId());
        notification.setNotificationType(
                record.getStatus() == AttendanceRecord.AttendanceStatus.ABSENT
                        ? Notification.NotificationType.ABSENCE
                        : Notification.NotificationType.LATE
        );
        notification.setMessage(String.format(
                "Dear Parent, your child %s was %s on %s for session %s.",
                studentName, record.getStatus().toString().toLowerCase(),
                record.getAttendanceDate(), record.getSession()
        ));
        notificationRepository.save(notification);

        AttendanceReminderToParent attendanceReminderToParent = AttendanceReminderToParent.builder()
                .parentEmail(parent.getEmail())
                .studentName(studentName)
                .status(record.getStatus().toString())
                .session(record.getSession().name())
                .slotNumber(record.getTimetableId() != null ? record.getTimetableId().intValue() : 0)
                .attendanceDate(record.getAttendanceDate())
                .build();
        kafkaTemplate.send("sendAttendanceReminderToParent", attendanceReminderToParent);
    }

    public List<AttendanceRecord> getStudentAttendanceHistory(String studentId, LocalDate startDate, LocalDate endDate) {
        return attendanceRecordRepository.findByStudentIdAndAttendanceDateBetween(studentId, startDate, endDate);
    }

    public List<RecordStudentAttendanceResponse> getClassAttendance(Long classId, LocalDate date) {
        List<AttendanceRecord> records = attendanceRecordRepository.findByClassIdAndAttendanceDate(classId, date);
        if (records.isEmpty()) {
            return List.of();
        }
        List<String> teacherIds = records.stream()
                .map(AttendanceRecord::getTeacherId)
                .distinct()
                .toList();
        List<String> studentIds = records.stream()
                .map(AttendanceRecord::getStudentId)
                .distinct()
                .toList();
        List<Long> subjectIds = records.stream()
                .map(AttendanceRecord::getSubjectId)
                .distinct()
                .toList();
        Map<Long, SubjectResponse> subjects = subjectClient.getSubjectByIds(subjectIds).getResult().stream()
                .collect(Collectors.toMap(SubjectResponse::getId, subject -> subject));

        Map<String, StudentResponse> teachers = studentClient.getUsersByIds(teacherIds).getResult().stream()
                .collect(Collectors.toMap(StudentResponse::getUserId, teacher -> teacher));

        Map<String, StudentResponse> students = studentClient.getUsersByIds(studentIds).getResult().stream()
                .collect(Collectors.toMap(StudentResponse::getUserId, student -> student));

        List<Long> timetableIds = records.stream()
                .map(AttendanceRecord::getTimetableId)
                .distinct()
                .toList();

        Map<Long, SlotDetailResponse> timetableMap = timetableClient.getTimetableByIds(timetableIds).getResult().stream()
                .collect(Collectors.toMap(SlotDetailResponse::getId, timetable -> timetable));
        ClassResponse classResponse = schoolClient.getClassInfo(classId).getResult();
        RoomResponse roomResponse = schoolClient.getRoomInfo(classId).getResult();
        return records.stream()
                .map(record -> RecordStudentAttendanceResponse.builder()
                        .id(record.getId())
                        .attendanceDate(record.getAttendanceDate())
                        .studentId(record.getStudentId())
                        .timetableId(record.getTimetableId())
                        .classId(record.getClassId())
                        .subjectName(subjects.get(record.getSubjectId()) != null ? subjects.get(record.getSubjectId()).getName() : null)
                        .fullName(students.get(record.getStudentId()) != null ? students.get(record.getStudentId()).getFullName() : null)
                        .subjectId(record.getSubjectId())
                        .teacherId(record.getTeacherId())
                        .session(record.getSession())
                        .status(record.getStatus())
                        .attendanceScore(record.getAttendanceScore())
                        .className(classResponse != null ? classResponse.getClassName() : null)
                        .roomName(roomResponse != null ? roomResponse.getRoomName() : null)
                        .roomId(roomResponse != null ? roomResponse.getRoomId() : null)
                        .teacherName(teachers.get(record.getTeacherId()) != null ? teachers.get(record.getTeacherId()).getFullName() : null)
                        .slot(timetableMap.get(record.getTimetableId()) != null ? timetableMap.get(record.getTimetableId()).getSlot() : 0)
                        .build())
                .collect(Collectors.toList());

    }

    public List<TeacherAttendanceHistoryResponse> getTeacherAttendanceHistory(String teacherId, LocalDate startDate, LocalDate endDate) {
        List<TeacherAttendance> teacherAttendances = teacherAttendanceRepository.findByTeacherIdAndAttendanceDateBetween(teacherId, startDate, endDate);
        TeacherResponse teacher = studentClient.getTeacherInfo(teacherId).getResult();
        if (teacher == null) {
            throw new AppException(ErrorCode.TEACHER_NOT_FOUND);
        }
        return teacherAttendances.stream()
                .map(attendance -> TeacherAttendanceHistoryResponse.builder()
                        .id(attendance.getId())
                        .attendanceDate(attendance.getAttendanceDate())
                        .status(attendance.getStatus())
                        .recordedById(attendance.getRecordedBy())
                        .recordedByName(teacher.getFullName())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void checkAbsencesAndSendWarnings(LocalDate date) {
        Map<String, String> config = getConfig();
        int threshold = Integer.parseInt(config.get("ABSENCE_THRESHOLD"));
        List<AttendanceRecord> absentRecords = attendanceRecordRepository.findAbsentRecordsByDateRange(
                date.minusDays(30), date
        );
        if (absentRecords.isEmpty()) {
            return; // No absences to process
        }
        List<String> ids = absentRecords.stream()
                .map(AttendanceRecord::getStudentId)
                .distinct()
                .toList();
        List<StudentResponse> students = studentClient.getUsersByIds(ids).getResult();

        if (students.isEmpty()) {
            return; // No students to notify
        }

        absentRecords.stream()
                .collect(Collectors.groupingBy(AttendanceRecord::getStudentId, Collectors.counting()))
                .forEach((studentId, absenceCount) -> {
                    if (absenceCount >= threshold) {
                        StudentResponse student = students.stream()
                                .filter(s -> s.getUserId().equals(studentId))
                                .findFirst()
                                .orElse(null);
                        if (student == null || student.getParents() == null || student.getParents().isEmpty()) {
                            return; // No parent to notify
                        }
                        student.getParents().forEach(parent -> {
                            if (parent.getIsNotificationOn()) {
                                sendWarningNotification(studentId, student.getFullName(), parent, absenceCount.intValue());
                            }
                        });
                    }
                });
    }

    private void sendWarningNotification(String studentId, String studentName, ParentResponse parent, int absenceCount) {
        Notification warning = new Notification();
        warning.setStudentId(studentId);
        warning.setParentEmail(parent.getEmail());
        warning.setNotificationType(Notification.NotificationType.WARNING);
        warning.setMessage(String.format(
                "Warning: Your child %s has %d absences in the last 30 days.",
                studentName, absenceCount
        ));
        notificationRepository.save(warning);

        WarningReminderToParent warningReminder = WarningReminderToParent.builder()
                .studentName(studentName)
                .parentEmail(parent.getEmail())
                .totalAbsences(String.valueOf(absenceCount))
                .build();

        kafkaTemplate.send("sendWarningReminderToParent", warningReminder);

    }

    @Transactional
    public AttendanceSummaryResponse generateAttendanceSummary(String studentId, AttendanceSummary.PeriodType periodType,
                                                               LocalDate startDate, LocalDate endDate) {
        List<AttendanceRecord> records = attendanceRecordRepository.findByStudentIdAndAttendanceDateBetween(studentId, startDate, endDate);
        AttendanceSummary summary = new AttendanceSummary();
        summary.setStudentId(studentId);
        summary.setClassId(records.isEmpty() ? null : records.get(0).getClassId());
        summary.setPeriodType(periodType);
        summary.setPeriodStartDate(startDate);
        summary.setPeriodEndDate(endDate);
        summary.setTotalSlots(records.size());
        summary.setPresentSlots((int) records.stream().filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT).count());
        summary.setAbsentSlots((int) records.stream().filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.ABSENT).count());
        summary.setLateSlots((int) records.stream().filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.LATE).count());
        summary.setEarlyLeaveSlots((int) records.stream().filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.EARLY_LEAVE).count());
        summary.setTotalScore(records.stream().mapToDouble(AttendanceRecord::getAttendanceScore).sum());
        attendanceSummaryRepository.save(summary);

        StudentResponse student = studentClient.getStudent(studentId).getResult();
        if (student == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        ClassResponse classResponse = schoolClient.getClassInfo(summary.getClassId()).getResult();
        if (classResponse == null) {
            throw new AppException(ErrorCode.CLASS_NOT_FOUND);
        }
        return AttendanceSummaryResponse.builder()
                .id(summary.getId())
                .studentId(summary.getStudentId())
                .fullName(student.getFullName())
                .classId(summary.getClassId())
                .className(classResponse.getClassName())
                .periodType(summary.getPeriodType())
                .periodStartDate(summary.getPeriodStartDate())
                .periodEndDate(summary.getPeriodEndDate())
                .totalSlots(summary.getTotalSlots())
                .presentSlots(summary.getPresentSlots())
                .absentSlots(summary.getAbsentSlots())
                .lateSlots(summary.getLateSlots())
                .earlyLeaveSlots(summary.getEarlyLeaveSlots())
                .totalScore(summary.getTotalScore())
                .build();
    }

    public List<RecordStudentAttendanceResponse> getAttendanceByTimetableId(Long timetableId) {
        List<AttendanceRecord> records = attendanceRecordRepository.findAllByTimetableId(timetableId);
        return records.stream()
                .map(this::mapToRecordResponse)
                .collect(Collectors.toList());
    }

    public List<RecordStudentAttendanceResponse> getStudentAttendanceHistoryBySubject(String studentId, Long subjectId, LocalDate startDate, LocalDate endDate) {
        List<AttendanceRecord> records = attendanceRecordRepository.findByStudentIdAndSubjectIdAndAttendanceDateBetween(
                studentId, subjectId, startDate, endDate);
        if (records.isEmpty()) {
            return List.of();
        }
        List<String> teacherIds = records.stream()
                .map(AttendanceRecord::getTeacherId)
                .distinct()
                .toList();
        List<Long> timetableIds = records.stream()
                .map(AttendanceRecord::getTimetableId)
                .distinct()
                .toList();
        Map<Long, SlotDetailResponse> timetableMap = timetableClient.getTimetableByIds(timetableIds).getResult().stream()
                .collect(Collectors.toMap(SlotDetailResponse::getId, timetable -> timetable));
        Map<String, StudentResponse> teachers = studentClient.getUsersByIds(teacherIds).getResult().stream()
                .collect(Collectors.toMap(StudentResponse::getUserId, teacher -> teacher));
        ClassResponse classResponse = schoolClient.getClassInfo(records.get(0).getClassId()).getResult();
        RoomResponse roomResponse = schoolClient.getRoomInfo(records.get(0).getClassId()).getResult();
        return records.stream()
                .map(record -> RecordStudentAttendanceResponse.builder()
                        .id(record.getId())
                        .attendanceDate(record.getAttendanceDate())
                        .studentId(record.getStudentId())
                        .timetableId(record.getTimetableId())
                        .classId(record.getClassId())
                        .subjectId(record.getSubjectId())
                        .teacherId(record.getTeacherId())
                        .session(record.getSession())
                        .status(record.getStatus())
                        .attendanceScore(record.getAttendanceScore())
                        .className(classResponse != null ? classResponse.getClassName() : null)
                        .roomName(roomResponse != null ? roomResponse.getRoomName() : null)
                        .roomId(roomResponse != null ? roomResponse.getRoomId() : null)
                        .teacherName(teachers.get(record.getTeacherId()) != null ? teachers.get(record.getTeacherId()).getFullName() : null)
                        .slot(timetableMap.get(record.getTimetableId()) != null ? timetableMap.get(record.getTimetableId()).getSlot() : 0)
                        .build())
                .collect(Collectors.toList());
    }

    public void initAttendanceByAllTimetableSlots() {
        List<SlotDetailResponse> timetableSlots = timetableClient.getAllTimetableSlots().getResult();
        if (timetableSlots.isEmpty()) {
            return; // No timetable slots to process
        }

        for (SlotDetailResponse slot : timetableSlots) {
            // Check if attendance record already exists for this slot
            if (!attendanceRecordRepository.existsByTimetableId(slot.getId())) {
                // Initialize attendance records for all students in the class
                List<StudentResponse> students = schoolClient.getListStudentByClassId(slot.getClassId()).getResult();
                for (StudentResponse student : students) {
                    AttendanceRecord record = AttendanceRecord.builder()
                            .studentId(student.getUserId())
                            .classId(slot.getClassId())
                            .subjectId(slot.getSubjectId())
                            .teacherId(slot.getTeacherId())
                            .attendanceDate(slot.getDate())
                            .session(slot.getMainSession())
                            .status(AttendanceRecord.AttendanceStatus.PRESENT)
                            .attendanceScore(10.0)
                            .timetableId(slot.getId())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    attendanceRecordRepository.save(record);
                }
            }
        }
    }

    public List<RecordStudentAttendanceResponse> getClassAttendanceBySubject(Long classId, Long subjectId, LocalDate date) {
        List<AttendanceRecord> records = attendanceRecordRepository.findByClassIdAndSubjectIdAndAttendanceDate(classId, subjectId, date);
        if (records.isEmpty()) {
            return List.of();
        }
        List<String> teacherIds = records.stream()
                .map(AttendanceRecord::getTeacherId)
                .distinct()
                .toList();
        List<String> studentIds = records.stream()
                .map(AttendanceRecord::getStudentId)
                .distinct()
                .toList();
        List<Long> timetableIds = records.stream()
                .map(AttendanceRecord::getTimetableId)
                .distinct()
                .toList();
        List<Long> subjectIds = records.stream()
                .map(AttendanceRecord::getSubjectId)
                .distinct()
                .toList();
        Map<String, StudentResponse> students = studentClient.getUsersByIds(studentIds).getResult().stream()
                .collect(Collectors.toMap(StudentResponse::getUserId, student -> student));
        Map<Long, SubjectResponse> subjects = subjectClient.getSubjectByIds(subjectIds).getResult().stream()
                .collect(Collectors.toMap(SubjectResponse::getId, subject -> subject));
        Map<Long, SlotDetailResponse> timetableMap = timetableClient.getTimetableByIds(timetableIds).getResult().stream()
                .collect(Collectors.toMap(SlotDetailResponse::getId, timetable -> timetable));
        Map<String, StudentResponse> teachers = studentClient.getUsersByIds(teacherIds).getResult().stream()
                .collect(Collectors.toMap(StudentResponse::getUserId, teacher -> teacher));
        ClassResponse classResponse = schoolClient.getClassInfo(classId).getResult();
        RoomResponse roomResponse = schoolClient.getRoomInfo(records.get(0).getClassId()).getResult();
        return records.stream()
                .map(record -> RecordStudentAttendanceResponse.builder()
                        .id(record.getId())
                        .attendanceDate(record.getAttendanceDate())
                        .studentId(record.getStudentId())
                        .timetableId(record.getTimetableId())
                        .classId(record.getClassId())
                        .subjectId(record.getSubjectId())
                        .subjectName(subjects.get(record.getSubjectId()) != null ? subjects.get(record.getSubjectId()).getName() : null)
                        .fullName(students.get(record.getStudentId()) != null ? students.get(record.getStudentId()).getFullName() : null)
                        .teacherId(record.getTeacherId())
                        .session(record.getSession())
                        .status(record.getStatus())
                        .attendanceScore(record.getAttendanceScore())
                        .className(classResponse != null ? classResponse.getClassName() : null)
                        .roomName(roomResponse != null ? roomResponse.getRoomName() : null)
                        .roomId(roomResponse != null ? roomResponse.getRoomId() : null)
                        .teacherName(teachers.get(record.getTeacherId()) != null ? teachers.get(record.getTeacherId()).getFullName() : null)
                        .slot(timetableMap.get(record.getTimetableId()) != null ? timetableMap.get(record.getTimetableId()).getSlot() : 0)
                        .build())
                .collect(Collectors.toList());
    }
}