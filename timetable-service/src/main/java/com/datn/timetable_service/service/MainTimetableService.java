package com.datn.timetable_service.service;

import com.datn.timetable_service.client.SchoolServiceClient;
import com.datn.timetable_service.client.SubjectServiceClient;
import com.datn.timetable_service.client.UserServiceClient;
import com.datn.timetable_service.dto.SchoolService.ClassResponse;
import com.datn.timetable_service.dto.SchoolService.RoomResponse;
import com.datn.timetable_service.dto.SchoolService.SemesterResponse;
import com.datn.timetable_service.dto.UserService.TeacherResponse;
import com.datn.timetable_service.dto.request.SlotSwapRequest;
import com.datn.timetable_service.dto.request.TimetableCloneRequest;
import com.datn.timetable_service.dto.request.TimetableCreateRequest;
import com.datn.timetable_service.dto.request.TimetableUpdateRequest;
import com.datn.timetable_service.dto.response.SlotDetailResponse;
import com.datn.timetable_service.dto.response.SlotDetailResponseV2;
import com.datn.timetable_service.dto.response.TimetableResponse;
import com.datn.timetable_service.dto.subject_service.RawTeacherSubjectClassResponse;
import com.datn.timetable_service.dto.subject_service.SubjectResponse;
import com.datn.timetable_service.exception.AppException;
import com.datn.timetable_service.exception.ErrorCode;
import com.datn.timetable_service.model.Timetable;
import com.datn.timetable_service.repository.TimetableRepository;
import com.datn.timetable_service.util.TimeSlotUtil;
import com.itextpdf.text.Font;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MainTimetableService {

    private final TimetableRepository timetableRepository;
    private final SchoolServiceClient schoolServiceClient;
    private final SubjectServiceClient subjectServiceClient;
    private final UserServiceClient userServiceClient;

    @Transactional
    public TimetableResponse createTimetableSlot(TimetableCreateRequest dto) {
        validateTimetableCreateDTO(dto);
        checkTimetableConstraints(dto);
        Timetable timetable = mapToEntity(dto);
        timetable = timetableRepository.save(timetable);
        log.info("Created timetable slot with ID: {}", timetable.getId());
        return mapToResponseDTO(timetable);
    }

    @Transactional
    public TimetableResponse updateTimetableSlot(Long id, TimetableUpdateRequest dto) {
        Timetable timetable = timetableRepository.findById(id)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.SLOT_NOT_FOUND));

        if (dto.getDayOfWeek() != null) timetable.setDayOfWeek(dto.getDayOfWeek());
        if (dto.getSlot() != null) timetable.setSlot(dto.getSlot());
        if (dto.getTeacherId() != null) timetable.setTeacherId(dto.getTeacherId());
        if (dto.getRoomId() != null) timetable.setRoomId(dto.getRoomId());
        if (dto.getDate() != null) timetable.setDate(dto.getDate());

        TimetableCreateRequest validationDTO = TimetableCreateRequest.builder()
                .classId(timetable.getClassId())
                .subjectId(timetable.getSubjectId())
                .teacherId(timetable.getTeacherId())
                .dayOfWeek(timetable.getDayOfWeek())
                .slot(timetable.getSlot())
                .roomId(timetable.getRoomId())
                .week(timetable.getWeek())
                .semesterId(timetable.getSemesterId())
                .schoolYearId(timetable.getSchoolYearId())
                .date(timetable.getDate())
                .build();

        checkTimetableConstraints(validationDTO, id);
        timetable = timetableRepository.save(timetable);
        log.info("Updated timetable slot with ID: {}", id);
        return mapToResponseDTO(timetable);
    }

    @Transactional
    public void softDeleteTimetableSlot(Long id) {
        Timetable timetable = timetableRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SLOT_NOT_FOUND));
        timetableRepository.softDeleteById(id);
        log.info("Soft deleted timetable slot with ID: {}", id);
    }

    @Transactional
    public void restoreTimetableSlot(Long id) {
        Timetable timetable = timetableRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SLOT_NOT_FOUND));
        if (!timetable.isDeleted()) {
            throw new AppException(ErrorCode.SLOT_IS_NOT_DELETED);
        }
        timetableRepository.restoreById(id);
        log.info("Restored timetable slot with ID: {}", id);
    }

    @Transactional
    public void permanentlyDeleteTimetableSlot(Long id) {
        Timetable timetable = timetableRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SLOT_NOT_FOUND));
        if (!timetable.isDeleted()) {
            throw new AppException(ErrorCode.SLOT_IS_NOT_DELETED);
        }
        timetableRepository.deleteById(id);
        log.info("Permanently deleted timetable slot with ID: {}", id);
    }

    public List<TimetableResponse> getClassSchedule(Long classId, Long semesterId, int week) {
        validateClassAndSemester(classId, semesterId);
        List<Timetable> slots = timetableRepository.findByClassIdAndSemesterIdAndWeekAndIsDeletedFalse(classId, semesterId, week);
        if (slots.isEmpty()) {
            log.info("No timetable slots found for class ID: {}, semester ID: {}, week: {}", classId, semesterId, week);
            return Collections.emptyList();
        }
        List<Long> subjectIdSet = slots.stream().map(Timetable::getSubjectId).collect(Collectors.toSet()).stream().toList();
        List<String> teacherIdSet = slots.stream().map(Timetable::getTeacherId).collect(Collectors.toSet()).stream().toList();
        RoomResponse roomResponse = schoolServiceClient.getRoomById(slots.get(0).getRoomId()).getResult();
        ClassResponse classResponse = schoolServiceClient.getClassById(classId).getResult();
        Map<Long, SubjectResponse> subjectMap = fetchSubjectsByIds(subjectIdSet);
        Map<String, TeacherResponse> teacherMap = fetchTeachersByIds(teacherIdSet);
        return slots.stream()
                .map(t -> mapToClassTimetableResponseDTO(t, classResponse, subjectMap, teacherMap, roomResponse))
                .collect(Collectors.toList());
    }

    private Map<Long, SubjectResponse> fetchSubjectsByIds(List<Long> subjectIds) {
        if (subjectIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<SubjectResponse> subjects = subjectServiceClient.getSubjectByIds(subjectIds).getResult();
        return subjects.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(SubjectResponse::getId, s -> s));
    }

    private Map<String, TeacherResponse> fetchTeachersByIds(List<String> teacherIds) {
        if (teacherIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<TeacherResponse> teachers = userServiceClient.getTeachersInfo(teacherIds).getResult();
        return teachers.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(TeacherResponse::getUserId, t -> t));
    }

    private TimetableResponse mapToClassTimetableResponseDTO(Timetable timetable,
                                                             ClassResponse classResponse,
                                                             Map<Long, SubjectResponse> subjectMap,
                                                             Map<String, TeacherResponse> teacherMap,
                                                             RoomResponse roomResponse) {
        SubjectResponse subject = subjectMap.get(timetable.getSubjectId());
        TeacherResponse teacher = teacherMap.get(timetable.getTeacherId());

        return TimetableResponse.builder()
                .id(timetable.getId())
                .classId(timetable.getClassId())
                .className(classResponse != null ? classResponse.getClassName() : null)
                .subjectId(timetable.getSubjectId())
                .subjectName(subject != null ? subject.getName() : null)
                .teacherId(timetable.getTeacherId())
                .teacherName(teacher != null ? teacher.getFullName() : null)
                .dayOfWeek(timetable.getDayOfWeek())
                .slot(timetable.getSlot())
                .roomId(timetable.getRoomId())
                .roomName(roomResponse != null ? roomResponse.getRoomName() : null)
                .week(timetable.getWeek())
                .semesterId(timetable.getSemesterId())
                .schoolYearId(timetable.getSchoolYearId())
                .date(timetable.getDate())
                .startTime(TimeSlotUtil.getStartTime(timetable.getSlot()))
                .endTime(TimeSlotUtil.getEndTime(timetable.getSlot()))
                .isDeleted(timetable.isDeleted())
                .build();
    }

    public SlotDetailResponse getSlotDetails(Long id) {
        Timetable timetable = timetableRepository.findById(id)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.SLOT_NOT_FOUND));
        return mapToSlotDetailResponseDTO(timetable);
    }

    public SlotDetailResponseV2 getSlotDetailsV2(Long id) {
        Timetable timetable = timetableRepository.findById(id)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.SLOT_NOT_FOUND));
        return SlotDetailResponseV2.builder()
                .id(timetable.getId())
                .classId(timetable.getClassId())
                .subjectId(timetable.getSubjectId())
                .teacherId(timetable.getTeacherId())
                .dayOfWeek(timetable.getDayOfWeek())
                .slot(timetable.getSlot())
                .roomId(timetable.getRoomId())
                .mainSession(timetable.getSlot() <= 5 ? "MORNING" : "AFTERNOON")
                .week(timetable.getWeek())
                .semesterId(timetable.getSemesterId())
                .schoolYearId(timetable.getSchoolYearId())
                .date(timetable.getDate())
                .startTime(TimeSlotUtil.getStartTime(timetable.getSlot()))
                .endTime(TimeSlotUtil.getEndTime(timetable.getSlot()))
                .build();
    }

    public List<TimetableResponse> getTeacherSchedule(String teacherId, Long semesterId, int week) {
        validateTeacherAndSemester(teacherId, semesterId);
        List<Timetable> slots = timetableRepository.findByTeacherIdAndSemesterIdAndWeekAndIsDeletedFalse(teacherId, semesterId, week);
        return slots.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    public List<TimetableResponse> getTimetableByFilters(Long semesterId, Integer week, Long classId, String teacherId,
                                                         LocalDate startDate, LocalDate endDate) {
        if (semesterId == null) {
            throw new AppException(ErrorCode.SEMESTER_ID_REQUIRED);
        }
        List<Timetable> slots;
        if (week != null && startDate != null && endDate != null) {
            slots = timetableRepository.findBySemesterIdAndWeekAndDateBetweenAndIsDeletedFalse(semesterId, week, startDate, endDate);
        } else if (week != null) {
            slots = timetableRepository.findBySemesterIdAndWeekAndIsDeletedFalse(semesterId, week);
        } else {
            slots = timetableRepository.findAll().stream()
                    .filter(t -> t.getSemesterId().equals(semesterId) && !t.isDeleted())
                    .collect(Collectors.toList());
        }

        if (classId != null) {
            slots = slots.stream().filter(t -> t.getClassId().equals(classId)).collect(Collectors.toList());
        }
        if (teacherId != null) {
            slots = slots.stream().filter(t -> t.getTeacherId().equals(teacherId)).collect(Collectors.toList());
        }

        return slots.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional
    public List<TimetableResponse> bulkCreateTimetableSlots(List<TimetableCreateRequest> dtos) {
        List<TimetableResponse> results = new ArrayList<>();
        for (TimetableCreateRequest dto : dtos) {
            try {
                results.add(createTimetableSlot(dto));
            } catch (Exception e) {
                log.error("Failed to create timetable slot: {}", e.getMessage());
                throw new AppException(ErrorCode.CREATE_SLOT_FAILED);
            }
        }
        return results;
    }

    public Map<String, Long> getTimetableSummary(Long semesterId, Integer week) {
        List<Timetable> slots = timetableRepository.findBySemesterIdAndWeekAndIsDeletedFalse(semesterId, week);
        Map<String, Long> summary = new HashMap<>();
        summary.put("totalSlots", (long) slots.size());
        summary.put("classes", slots.stream().map(Timetable::getClassId).distinct().count());
        summary.put("teachers", slots.stream().map(Timetable::getTeacherId).distinct().count());
        return summary;
    }

    private void validateTimetableCreateDTO(TimetableCreateRequest dto) {
        ClassResponse clazz = schoolServiceClient.getClassById(dto.getClassId()).getResult();
        if (clazz == null) {
            throw new RuntimeException("Invalid class ID: " + dto.getClassId());
        }
        SubjectResponse subject = subjectServiceClient.getSubjectById(dto.getSubjectId()).getResult();
        if (subject == null) {
            throw new RuntimeException("Invalid subject ID: " + dto.getSubjectId());
        }
        TeacherResponse teacher = userServiceClient.getTeacherInfo(dto.getTeacherId()).getResult();
        if (teacher == null) {
            throw new RuntimeException("Invalid teacher ID: " + dto.getTeacherId());
        }
        RoomResponse room = schoolServiceClient.getRoomById(dto.getRoomId()).getResult();
        if (room == null) {
            throw new RuntimeException("Invalid room ID: " + dto.getRoomId());
        }
        SemesterResponse semester = schoolServiceClient.getSemesterById(dto.getSemesterId()).getResult();
        if (semester == null || !dto.getDate().isAfter(semester.getStartDate().minusDays(1)) ||
                !dto.getDate().isBefore(semester.getEndDate().plusDays(1))) {
            throw new RuntimeException("Date is outside semester range or invalid semester ID");
        }
        List<RawTeacherSubjectClassResponse> teacherSubjects = subjectServiceClient.getTeacherSubjectByClass(dto.getClassId()).getResult();
        boolean validAssignment = teacherSubjects.stream()
                .anyMatch(tsc -> tsc.getTeacherId().equals(dto.getTeacherId()) && tsc.getSubjectId().equals(dto.getSubjectId()));
        if (!validAssignment) {
            throw new RuntimeException("Teacher " + dto.getTeacherId() + " is not assigned to subject " +
                    dto.getSubjectId() + " for class " + dto.getClassId());
        }
    }

    private void checkTimetableConstraints(TimetableCreateRequest dto, Long... excludeId) {
        List<Timetable> classConflicts = timetableRepository.findByClassIdAndSemesterIdAndWeekAndDayOfWeekAndSlot(
                dto.getClassId(), dto.getSemesterId(), dto.getWeek(), dto.getDayOfWeek(), dto.getSlot());
        if (classConflicts.stream().anyMatch(t -> !t.isDeleted() && (excludeId.length == 0 || !t.getId().equals(excludeId[0])))) {
            throw new RuntimeException("Class " + dto.getClassId() + " is already scheduled at day " +
                    dto.getDayOfWeek() + ", slot " + dto.getSlot());
        }

        List<Timetable> teacherConflicts = timetableRepository.findByTeacherIdAndSemesterIdAndWeekAndDayOfWeekAndSlot(
                dto.getTeacherId(), dto.getSemesterId(), dto.getWeek(), dto.getDayOfWeek(), dto.getSlot());
        if (teacherConflicts.stream().anyMatch(t -> !t.isDeleted() && (excludeId.length == 0 || !t.getId().equals(excludeId[0])))) {
            throw new RuntimeException("Teacher " + dto.getTeacherId() + " is already teaching at day " +
                    dto.getDayOfWeek() + ", slot " + dto.getSlot());
        }

        List<Timetable> roomConflicts = timetableRepository.findByRoomIdAndSemesterIdAndWeekAndDayOfWeekAndSlot(
                dto.getRoomId(), dto.getSemesterId(), dto.getWeek(), dto.getDayOfWeek(), dto.getSlot());
        if (roomConflicts.stream().anyMatch(t -> !t.isDeleted() && (excludeId.length == 0 || !t.getId().equals(excludeId[0])))) {
            throw new RuntimeException("Room " + dto.getRoomId() + " is already booked at day " +
                    dto.getDayOfWeek() + ", slot " + dto.getSlot());
        }
    }

    private void validateClassAndSemester(Long classId, Long semesterId) {
        ClassResponse clazz = schoolServiceClient.getClassById(classId).getResult();
        if (clazz == null) {
            throw new RuntimeException("Invalid class ID: " + classId);
        }
        SemesterResponse semester = schoolServiceClient.getSemesterById(semesterId).getResult();
        if (semester == null) {
            throw new RuntimeException("Invalid semester ID: " + semesterId);
        }
    }

    private void validateTeacherAndSemester(String teacherId, Long semesterId) {
        TeacherResponse teacher = userServiceClient.getTeacherInfo(teacherId).getResult();
        if (teacher == null) {
            throw new RuntimeException("Invalid teacher ID: " + teacherId);
        }
        SemesterResponse semester = schoolServiceClient.getSemesterById(semesterId).getResult();
        if (semester == null) {
            throw new RuntimeException("Invalid semester ID: " + semesterId);
        }
    }

    private Timetable mapToEntity(TimetableCreateRequest dto) {
        Timetable timetable = new Timetable();
        timetable.setClassId(dto.getClassId());
        timetable.setSubjectId(dto.getSubjectId());
        timetable.setTeacherId(dto.getTeacherId());
        timetable.setDayOfWeek(dto.getDayOfWeek());
        timetable.setSlot(dto.getSlot());
        timetable.setRoomId(dto.getRoomId());
        timetable.setWeek(dto.getWeek());
        timetable.setSemesterId(dto.getSemesterId());
        timetable.setSchoolYearId(dto.getSchoolYearId());
        timetable.setDate(dto.getDate());
        return timetable;
    }

    private TimetableResponse mapToResponseDTO(Timetable timetable) {
        ClassResponse clazz = schoolServiceClient.getClassById(timetable.getClassId()).getResult();
        SubjectResponse subject = subjectServiceClient.getSubjectById(timetable.getSubjectId()).getResult();
        TeacherResponse teacher = userServiceClient.getTeacherInfo(timetable.getTeacherId()).getResult();
        RoomResponse room = schoolServiceClient.getRoomById(timetable.getRoomId()).getResult();

        return TimetableResponse.builder()
                .id(timetable.getId())
                .classId(timetable.getClassId())
                .className(clazz != null ? clazz.getClassName() : null)
                .subjectId(timetable.getSubjectId())
                .subjectName(subject != null ? subject.getName() : null)
                .teacherId(timetable.getTeacherId())
                .teacherName(teacher != null ? teacher.getFullName() : null)
                .dayOfWeek(timetable.getDayOfWeek())
                .slot(timetable.getSlot())
                .roomId(timetable.getRoomId())
                .roomName(room != null ? room.getRoomName() : null)
                .week(timetable.getWeek())
                .semesterId(timetable.getSemesterId())
                .schoolYearId(timetable.getSchoolYearId())
                .date(timetable.getDate())
                .startTime(TimeSlotUtil.getStartTime(timetable.getSlot()))
                .endTime(TimeSlotUtil.getEndTime(timetable.getSlot()))
                .isDeleted(timetable.isDeleted())
                .build();
    }

    private SlotDetailResponse mapToSlotDetailResponseDTO(Timetable timetable) {
        ClassResponse clazz = schoolServiceClient.getClassById(timetable.getClassId()).getResult();
        SubjectResponse subject = subjectServiceClient.getSubjectById(timetable.getSubjectId()).getResult();
        TeacherResponse teacher = userServiceClient.getTeacherInfo(timetable.getTeacherId()).getResult();
        RoomResponse room = schoolServiceClient.getRoomById(timetable.getRoomId()).getResult();

        return SlotDetailResponse.builder()
                .id(timetable.getId())
                .classId(timetable.getClassId())
                .className(clazz != null ? clazz.getClassName() : null)
                .subjectId(timetable.getSubjectId())
                .subjectName(subject != null ? subject.getName() : null)
                .teacherId(timetable.getTeacherId())
                .teacherName(teacher != null ? teacher.getFullName() : null)
                .teacherEmail(teacher != null ? teacher.getEmail() : null)
                .teacherPhone(teacher != null ? teacher.getPhoneNumber() : null)
                .dayOfWeek(timetable.getDayOfWeek())
                .slot(timetable.getSlot())
                .roomId(timetable.getRoomId())
                .roomName(room != null ? room.getRoomName() : null)
                .week(timetable.getWeek())
                .semesterId(timetable.getSemesterId())
                .schoolYearId(timetable.getSchoolYearId())
                .date(timetable.getDate())
                .startTime(TimeSlotUtil.getStartTime(timetable.getSlot()))
                .endTime(TimeSlotUtil.getEndTime(timetable.getSlot()))
                .build();
    }

    public byte[] exportTimetableToExcel(Long semesterId, int week, Long classId, String teacherId) {
        List<Timetable> slots = getFilteredSlots(semesterId, week, classId, teacherId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Timetable");

            CellStyle wrapTextStyle = workbook.createCellStyle();
            wrapTextStyle.setWrapText(true);
            wrapTextStyle.setVerticalAlignment(VerticalAlignment.TOP);

            String[] daysOfWeek = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Session");
            headerRow.createCell(1).setCellValue("Slot");

            LocalDate semesterStartDate = schoolServiceClient.getSemesterById(semesterId).getResult().getStartDate();

            LocalDate firstMonday = semesterStartDate.with(DayOfWeek.MONDAY);
            if (semesterStartDate.getDayOfWeek() != DayOfWeek.MONDAY) {
                firstMonday = semesterStartDate.plusDays(8 - semesterStartDate.getDayOfWeek().getValue());
            }

            LocalDate mondayOfWeek = firstMonday.plusWeeks(week - 1);
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");

            for (int i = 0; i < daysOfWeek.length; i++) {
                String date = mondayOfWeek.plusDays(i).format(dateFormatter);
                headerRow.createCell(i + 2).setCellValue(daysOfWeek[i] + " " + date);
            }

            TimetableResponse[][] timetableMap = new TimetableResponse[10][6];
            for (Timetable slot : slots) {
                TimetableResponse dto = mapToResponseDTO(slot);
                int slotIndex = dto.getSlot() - 1; // Slot 1-based to 0-based
                int dayIndex = dto.getDayOfWeek() - 1; // Day 1-based (Monday) to 0-based
                if (slotIndex >= 0 && slotIndex < 10 && dayIndex >= 0 && dayIndex < 6) {
                    timetableMap[slotIndex][dayIndex] = dto;
                }
            }

            for (int slot = 1; slot <= 10; slot++) {
                Row row = sheet.createRow(slot);
                row.setHeight((short) (80 * 20)); // Set row height to ~80 points for 4 lines
                boolean isMorning = slot <= 5;
                String session = isMorning ? "Morning" : "Afternoon";

                Cell sessionCell = row.createCell(0);
                sessionCell.setCellValue(slot == 1 || slot == 6 ? session : "");
                if (slot == 1 || slot == 6) {
                    sheet.addMergedRegion(new CellRangeAddress(slot, slot + 4, 0, 0));
                }

                row.createCell(1).setCellValue(slot);

                for (int day = 0; day < 6; day++) {
                    Cell cell = row.createCell(day + 2);
                    cell.setCellStyle(wrapTextStyle);
                    TimetableResponse dto = timetableMap[slot - 1][day];
                    if (dto != null) {
                        String content = String.format(
                                "%s\n%s\nRoom: %s\n%s - %s",
                                dto.getSubjectName() != null ? dto.getSubjectName() : "N/A",
                                dto.getTeacherName() != null ? dto.getTeacherName() : "N/A",
                                dto.getRoomName() != null ? dto.getRoomName() : "N/A",
                                dto.getStartTime() != null ? dto.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "N/A",
                                dto.getEndTime() != null ? dto.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "N/A"
                        );
                        cell.setCellValue(content);
                    } else {
                        cell.setCellValue("-");
                    }
                }
            }

            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }

            for (int i = 2; i < 8; i++) {
                if (sheet.getColumnWidth(i) < 5000) {
                    sheet.setColumnWidth(i, 5000);
                }
            }

            workbook.write(out);
            log.info("Exported timetable to Excel for semester {} week {} classId {}", semesterId, week, classId);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to export Excel: {}", e.getMessage());
            throw new RuntimeException("Failed to export timetable to Excel");
        }
    }

    @Transactional
    public void swapTimetableSlots(SlotSwapRequest dto) {
        Timetable slot1 = timetableRepository.findById(dto.getSlotId1())
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.SLOT_NOT_FOUND));
        Timetable slot2 = timetableRepository.findById(dto.getSlotId2())
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.SLOT_NOT_FOUND));

        if (!slot1.getSemesterId().equals(slot2.getSemesterId()) || slot1.getWeek() != slot2.getWeek()) {
            throw new RuntimeException("Slots must belong to the same semester and week");
        }

        int day1 = slot1.getDayOfWeek(), slotNum1 = slot1.getSlot();
        int day2 = slot2.getDayOfWeek(), slotNum2 = slot2.getSlot();
        LocalDate date1 = slot1.getDate(), date2 = slot2.getDate();

        slot1.setDayOfWeek(day2);
        slot1.setSlot(slotNum2);
        slot1.setDate(date2);
        slot2.setDayOfWeek(day1);
        slot2.setSlot(slotNum1);
        slot2.setDate(date1);

        checkTimetableConstraints(mapToCreateDTO(slot1), slot1.getId());
        checkTimetableConstraints(mapToCreateDTO(slot2), slot2.getId());

        timetableRepository.saveAll(Arrays.asList(slot1, slot2));
        log.info("Swapped timetable slots {} and {}", dto.getSlotId1(), dto.getSlotId2());
    }

    @Transactional
    public List<TimetableResponse> cloneTimetable(TimetableCloneRequest dto) {
        List<Timetable> sourceSlots = timetableRepository.findBySemesterIdAndWeekAndIsDeletedFalse(dto.getSourceSemesterId(), dto.getSourceWeek());

        if (sourceSlots.isEmpty()) {
            log.warn("No timetable slots found for semester {} and week {}",
                    dto.getSourceSemesterId(), dto.getSourceWeek());
            return Collections.emptyList();
        }
        List<Timetable> newSlots = new ArrayList<>();

        for (Timetable slot : sourceSlots) {
            Timetable newSlot = new Timetable(slot);
            newSlot.setId(null); // Reset ID for new slot
            newSlot.setSemesterId(dto.getTargetSemesterId());
            newSlot.setWeek(dto.getTargetWeek());
            newSlot.setDate(dto.getTargetWeekStartDate().plusDays(slot.getDayOfWeek() - 1));
            checkTimetableConstraints(mapToCreateDTO(newSlot));
            newSlots.add(newSlot);
        }

        newSlots = timetableRepository.saveAll(newSlots);
        log.info("Cloned {} timetable slots from semester {} week {} to semester {} week {}",
                newSlots.size(), dto.getSourceSemesterId(), dto.getSourceWeek(), dto.getTargetSemesterId(), dto.getTargetWeek());
        return newSlots.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    private List<Timetable> getFilteredSlots(Long semesterId, int week, Long classId, String teacherId) {
        List<Timetable> slots = timetableRepository.findBySemesterIdAndWeekAndIsDeletedFalse(semesterId, week);
        if (classId != null) {
            slots = slots.stream().filter(t -> t.getClassId().equals(classId)).collect(Collectors.toList());
        }
        if (teacherId != null) {
            slots = slots.stream().filter(t -> t.getTeacherId().equals(teacherId)).collect(Collectors.toList());
        }
        return slots;
    }

    public byte[] generateTimetablePDF(Long semesterId, int week, Long classId, String teacherId) throws IOException {
        List<Timetable> slots = getFilteredSlots(semesterId, week, classId, teacherId);
        ClassResponse clazz = classId != null ? schoolServiceClient.getClassById(classId).getResult() : null;
        String className = clazz != null ? clazz.getClassName() : "All Classes";

        LocalDate semesterStartDate = schoolServiceClient.getSemesterById(semesterId).getResult().getStartDate();
        LocalDate firstMonday = semesterStartDate.with(DayOfWeek.MONDAY);
        if (semesterStartDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            firstMonday = semesterStartDate.plusDays(8 - semesterStartDate.getDayOfWeek().getValue());
        }

        LocalDate mondayOfWeek = firstMonday.plusWeeks(week - 1);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String[] dateHeaders = new String[6];
        for (int i = 0; i < 6; i++) {
            dateHeaders[i] = daysOfWeek[i].toUpperCase() + " " + mondayOfWeek.plusDays(i).format(dateFormatter);
        }

        // Initialize iText PDF
        Document document = new Document(PageSize.A4.rotate()); // Landscape orientation
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Set font for Vietnamese support
            BaseFont baseFont = BaseFont.createFont("fonts/NotoSans-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font font = new Font(baseFont, 8); // Equivalent to \scriptsize
            Font headerFont = new Font(baseFont, 12, Font.BOLD);

            // Title and metadata
            Paragraph title = new Paragraph("Weekly Timetable", headerFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("Class: " + className, font));
            document.add(new Paragraph("Semester: " + semesterId + ", Week: " + week, font));
            document.add(new Paragraph("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), font));
            document.add(new Paragraph(" ")); // Spacer

            // Create table
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 1, 2, 2, 2, 2, 2, 2}); // Adjust column widths

            // Header row
            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerCell.setPadding(5);
            headerCell.setPhrase(new Phrase("Session", headerFont));
            table.addCell(headerCell);
            headerCell.setPhrase(new Phrase("Slot", headerFont));
            table.addCell(headerCell);
            for (String header : dateHeaders) {
                headerCell.setPhrase(new Phrase(header, headerFont));
                table.addCell(headerCell);
            }

            // Data rows
            TimetableResponse[][] timetableMap = new TimetableResponse[10][6];
            for (Timetable slot : slots) {
                TimetableResponse dto = mapToResponseDTO(slot);
                int slotIndex = dto.getSlot() - 1;
                int dayIndex = dto.getDayOfWeek() - 1;
                if (slotIndex >= 0 && slotIndex < 10 && dayIndex >= 0 && dayIndex < 6) {
                    timetableMap[slotIndex][dayIndex] = dto;
                }
            }

            for (int slot = 1; slot <= 10; slot++) {
                boolean isMorning = slot <= 5;
                String session = isMorning ? "Morning" : "Afternoon";
                if (slot == 1 || slot == 6) {
                    PdfPCell sessionCell = new PdfPCell(new Phrase(session, font));
                    sessionCell.setRowspan(5);
                    sessionCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    sessionCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    sessionCell.setFixedHeight(56); // ~4cm
                    table.addCell(sessionCell);
                }
                PdfPCell slotCell = new PdfPCell(new Phrase(String.valueOf(slot), font));
                slotCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                slotCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                slotCell.setFixedHeight(56);
                table.addCell(slotCell);
                for (int day = 0; day < 6; day++) {
                    TimetableResponse dto = timetableMap[slot - 1][day];
                    PdfPCell cell = new PdfPCell();
                    cell.setFixedHeight(56); // ~4cm
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setPadding(5);
                    if (dto != null) {
                        Phrase content = new Phrase();
                        content.add(new Chunk(dto.getSubjectName() != null ? dto.getSubjectName() : "N/A", font));
                        content.add(Chunk.NEWLINE);
                        content.add(new Chunk(dto.getTeacherName() != null ? dto.getTeacherName() : "N/A", font));
                        content.add(Chunk.NEWLINE);
                        content.add(new Chunk("Room: " + (dto.getRoomName() != null ? dto.getRoomName() : "N/A"), font));
                        content.add(Chunk.NEWLINE);
                        content.add(new Chunk(
                                (dto.getStartTime() != null ? dto.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "N/A") +
                                        " - " +
                                        (dto.getEndTime() != null ? dto.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "N/A"), font));
                        cell.setPhrase(content);
                    } else {
                        cell.setPhrase(new Phrase("-", font));
                    }
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("Failed to generate PDF with iText: {}", e.getMessage());
            throw new IOException("Failed to generate PDF with iText", e);
        }
    }

    private TimetableCreateRequest mapToCreateDTO(Timetable timetable) {
        return TimetableCreateRequest.builder()
                .classId(timetable.getClassId())
                .subjectId(timetable.getSubjectId())
                .teacherId(timetable.getTeacherId())
                .dayOfWeek(timetable.getDayOfWeek())
                .slot(timetable.getSlot())
                .roomId(timetable.getRoomId())
                .week(timetable.getWeek())
                .semesterId(timetable.getSemesterId())
                .schoolYearId(timetable.getSchoolYearId())
                .date(timetable.getDate())
                .build();
    }

    public SlotDetailResponseV2 mapToSlotDetailResponseV2(Timetable timetable) {
        return SlotDetailResponseV2.builder()
                .id(timetable.getId())
                .classId(timetable.getClassId())
                .subjectId(timetable.getSubjectId())
                .teacherId(timetable.getTeacherId())
                .dayOfWeek(timetable.getDayOfWeek())
                .slot(timetable.getSlot())
                .roomId(timetable.getRoomId())
                .mainSession(timetable.getSlot() <= 5 ? "MORNING" : "AFTERNOON")
                .week(timetable.getWeek())
                .semesterId(timetable.getSemesterId())
                .schoolYearId(timetable.getSchoolYearId())
                .date(timetable.getDate())
                .startTime(TimeSlotUtil.getStartTime(timetable.getSlot()))
                .endTime(TimeSlotUtil.getEndTime(timetable.getSlot()))
                .build();
    }

    public List<SlotDetailResponseV2> getSlotDetailsByIdsV2(List<Long> ids) {
        List<Timetable> slots = timetableRepository.findAllById(ids).stream()
                .filter(t -> !t.isDeleted())
                .toList();
        if (slots.isEmpty()) {
            throw new AppException(ErrorCode.SLOT_NOT_FOUND);
        }

        return slots.stream()
                .map(this::mapToSlotDetailResponseV2)
                .collect(Collectors.toList());
    }

    public List<SlotDetailResponseV2> getAllTimetableSlots() {
        List<Timetable> slots = timetableRepository.findAll().stream()
                .filter(t -> !t.isDeleted())
                .toList();
        if (slots.isEmpty()) {
            return Collections.emptyList();
        }

        return slots.stream()
                .map(this::mapToSlotDetailResponseV2)
                .collect(Collectors.toList());
    }

    public void deleteTimetableSlot(Long id) {
        timetableRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SLOT_NOT_FOUND));
        timetableRepository.deleteById(id);
    }
}
