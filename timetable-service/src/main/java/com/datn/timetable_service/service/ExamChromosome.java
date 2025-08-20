package com.datn.timetable_service.service;

import com.datn.timetable_service.dto.SchoolService.ClassResponse;
import com.datn.timetable_service.dto.SchoolService.RoomResponse;
import com.datn.timetable_service.dto.UserService.TeacherResponse;
import com.datn.timetable_service.dto.subject_service.SubjectResponse;
import com.datn.timetable_service.dto.subject_service.TeacherSubjectClassResponse;
import com.datn.timetable_service.model.ExamSchedule;
import com.datn.timetable_service.model.SubjectExamInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Data
@NoArgsConstructor
public class ExamChromosome {
    private List<ExamSchedule> examSchedule = new ArrayList<>();
    private double fitness;
    private Map<String, Set<String>> teacherSlots = new HashMap<>();
    private Map<String, Set<String>> roomSlots = new HashMap<>();
    private Map<String, Set<String>> classSlots = new HashMap<>();

    public ExamChromosome(ExamChromosome chromosome) {
        this.examSchedule = chromosome.getExamSchedule().stream()
                .map(ExamSchedule::new)
                .collect(Collectors.toList());
        this.fitness = chromosome.getFitness();
        this.teacherSlots = new HashMap<>(chromosome.getTeacherSlots());
        this.roomSlots = new HashMap<>(chromosome.getRoomSlots());
        this.classSlots = new HashMap<>(chromosome.getClassSlots());
    }

    public void assignExamSlots(List<ClassResponse> classes,
                                Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects,
                                List<TeacherResponse> teachers, List<RoomResponse> rooms,
                                Long semesterId, Long schoolYearId, LocalDate startDate, LocalDate endDate,
                                List<SubjectExamInfo> subjectExamInfos) {
        log.debug("Assigning exam slots for all classes");

        if (classes == null || classes.isEmpty() || classToTeacherSubjects == null || teachers == null || teachers.isEmpty() ||
                rooms == null || rooms.isEmpty() || subjectExamInfos == null || subjectExamInfos.isEmpty()) {
            log.error("Invalid input data for assignExamSlots: classes.size={}, teachers.size={}, rooms.size={}, subjectExamInfos.size={}, classToTeacherSubjects.size={}",
                    classes == null ? 0 : classes.size(), teachers == null ? 0 : teachers.size(),
                    rooms == null ? 0 : rooms.size(), subjectExamInfos == null ? 0 : subjectExamInfos.size(),
                    classToTeacherSubjects == null ? 0 : classToTeacherSubjects.size());
            return;
        }

        log.debug("Input data: classes.size={}, teachers.size={}, rooms.size={}, subjectExamInfos.size={}, classToTeacherSubjects.size={}, examTimes.size={}",
                classes.size(), teachers.size(), rooms.size(), subjectExamInfos.size(),
                classToTeacherSubjects.size(), ExamScheduleService.EXAM_TIMES.size());

        // Log room assignments
        classes.forEach(c -> log.debug("Class {} assigned to room {}", c.getClassId(), c.getRoomResponse().getRoomId()));

        Random rand = new Random();
        Map<Long, List<ClassResponse>> subjectToClasses = new HashMap<>();

        // Group classes by subject
        for (ClassResponse clazz : classes) {
            if (clazz.getRoomResponse() == null) {
                log.warn("Class {} has null RoomResponse, skipping", clazz.getClassId());
                continue;
            }
            List<TeacherSubjectClassResponse> teacherSubjects = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList());
            if (teacherSubjects.isEmpty()) {
                log.warn("No teacher subjects for class {}", clazz.getClassId());
                continue;
            }
            for (TeacherSubjectClassResponse ts : teacherSubjects) {
                SubjectResponse subject = ts.getSubject();
                if (subject == null || subject.getName().equals("Class Activities") || subject.getName().equals("Flag Salute Session")) {
                    continue;
                }
                subjectToClasses.computeIfAbsent(subject.getId(), k -> new ArrayList<>()).add(clazz);
            }
        }

        log.debug("Subjects to schedule: subjectToClasses.size={}", subjectToClasses.size());

        // Assign slots for each subject
        for (Map.Entry<Long, List<ClassResponse>> entry : subjectToClasses.entrySet()) {
            Long subjectId = entry.getKey();
            List<ClassResponse> subjectClasses = entry.getValue();
            SubjectExamInfo examInfo = subjectExamInfos.stream()
                    .filter(info -> info.getSubjectId().equals(subjectId))
                    .findFirst()
                    .orElse(null);
            if (examInfo == null) {
                log.warn("No exam info for subjectId {}", subjectId);
                continue;
            }

            List<LocalDate> examDates = new ArrayList<>();
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                examDates.add(date);
            }
            Collections.shuffle(examDates, rand);

            // Try to assign all classes together first
            boolean assignedTogether = false;
            for (LocalDate examDate : examDates) {
                for (LocalTime examTime : ExamScheduleService.EXAM_TIMES) {
                    boolean validForAll = true;
                    Map<ClassResponse, TeacherResponse> classTeachers = new HashMap<>();
                    for (ClassResponse clazz : subjectClasses) {
                        SubjectResponse subject = classToTeacherSubjects.get(clazz).stream()
                                .map(TeacherSubjectClassResponse::getSubject)
                                .filter(s -> s.getId().equals(subjectId))
                                .findFirst()
                                .orElse(null);
                        if (subject == null) {
                            log.warn("Subject not found for subjectId {} in class {}", subjectId, clazz.getClassId());
                            validForAll = false;
                            break;
                        }
                        TeacherResponse teacher = getRandomTeacher(teachers);
                        if (teacher == null) {
                            validForAll = false;
                            break;
                        }
                        if (!isSlotValid(clazz, subject, examDate, examTime, teacher, clazz.getRoomResponse(), examInfo)) {
                            validForAll = false;
                            break;
                        }
                        classTeachers.put(clazz, teacher);
                    }

                    if (validForAll) {
                        for (ClassResponse clazz : subjectClasses) {
                            SubjectResponse subject = classToTeacherSubjects.get(clazz).stream()
                                    .map(TeacherSubjectClassResponse::getSubject)
                                    .filter(s -> s.getId().equals(subjectId))
                                    .findFirst()
                                    .orElse(null);
                            if (subject == null) continue;
                            TeacherResponse teacher = classTeachers.get(clazz);
                            ExamSchedule examSlot = new ExamSchedule();
                            examSlot.setClassId(clazz.getClassId());
                            examSlot.setSubjectId(subjectId);
                            examSlot.setExamDate(examDate);
                            examSlot.setExamTime(examTime);
                            examSlot.setRoomId(clazz.getRoomResponse().getRoomId());
                            examSlot.setTeacherId(teacher.getUserId());
                            examSlot.setSemesterId(semesterId);
                            examSlot.setSchoolYearId(schoolYearId);
                            examSchedule.add(examSlot);
                            updateSlotMaps(examSlot);
                            log.debug("Assigned {} for class {} on {} at {} with teacher {} in room {}",
                                    subject.getName(), clazz.getClassName(), examDate, examTime, teacher.getUserId(), clazz.getRoomResponse().getRoomId());
                        }
                        assignedTogether = true;
                        break;
                    }
                }
                if (assignedTogether) break;
            }

            // If assigning together fails, try assigning each class individually
            if (!assignedTogether) {
                log.warn("Failed to assign subjectId {} together, trying individually", subjectId);
                for (ClassResponse clazz : subjectClasses) {
                    SubjectResponse subject = classToTeacherSubjects.get(clazz).stream()
                            .map(TeacherSubjectClassResponse::getSubject)
                            .filter(s -> s.getId().equals(subjectId))
                            .findFirst()
                            .orElse(null);
                    if (subject == null) {
                        log.warn("Subject not found for subjectId {} in class {}", subjectId, clazz.getClassId());
                        continue;
                    }
                    boolean assigned = false;
                    Collections.shuffle(examDates, rand);
                    for (LocalDate examDate : examDates) {
                        for (LocalTime examTime : ExamScheduleService.EXAM_TIMES) {
                            TeacherResponse teacher = getRandomTeacher(teachers);
                            if (teacher == null) continue;
                            if (isSlotValid(clazz, subject, examDate, examTime, teacher, clazz.getRoomResponse(), examInfo)) {
                                ExamSchedule examSlot = new ExamSchedule();
                                examSlot.setClassId(clazz.getClassId());
                                examSlot.setSubjectId(subjectId);
                                examSlot.setExamDate(examDate);
                                examSlot.setExamTime(examTime);
                                examSlot.setRoomId(clazz.getRoomResponse().getRoomId());
                                examSlot.setTeacherId(teacher.getUserId());
                                examSlot.setSemesterId(semesterId);
                                examSlot.setSchoolYearId(schoolYearId);
                                examSchedule.add(examSlot);
                                updateSlotMaps(examSlot);
                                log.debug("Assigned {} for class {} on {} at {} (individual) with teacher {} in room {}",
                                        subject.getName(), clazz.getClassName(), examDate, examTime, teacher.getUserId(), clazz.getRoomResponse().getRoomId());
                                assigned = true;
                                break;
                            }
                        }
                        if (assigned) break;
                    }
                    if (!assigned) {
                        log.warn("Failed to assign exam slot for subjectId {} in class {}", subjectId, clazz.getClassId());
                    }
                }
            }
        }
    }

    boolean isSlotValid(ClassResponse clazz, SubjectResponse subject, LocalDate examDate, LocalTime examTime,
                        TeacherResponse teacher, RoomResponse room, SubjectExamInfo examInfo) {
        if (clazz == null || subject == null || teacher == null || room == null || examInfo == null) {
            log.warn("Null input in isSlotValid: clazz={}, subject={}, teacher={}, room={}, examInfo={}",
                    clazz, subject, teacher, room, examInfo);
            return false;
        }

        String classKey = clazz.getClassId() + "_" + examDate + "_" + examTime.toString();
        String teacherKey = teacher.getUserId() + "_" + examDate + "_" + examTime.toString();
        String roomKey = room.getRoomId() + "_" + examDate + "_" + examTime.toString();

        // Check time frame and duration
        LocalTime endTime = examTime.plusMinutes(examInfo.getDuration());
        boolean withinTimeFrame = (examTime.isBefore(ExamScheduleService.MORNING_END) &&
                endTime.isBefore(ExamScheduleService.MORNING_END.plusMinutes(1))) ||
                (examTime.isAfter(LocalTime.of(12, 0)) &&
                        endTime.isBefore(ExamScheduleService.AFTERNOON_END.plusMinutes(1)));
        if (!withinTimeFrame) {
            log.debug("Time frame invalid: classId={}, subjectId={}, date={}, time={}, endTime={}, duration={}, morningEnd={}, afternoonEnd={}",
                    clazz.getClassId(), subject.getId(), examDate, examTime, endTime, examInfo.getDuration(),
                    ExamScheduleService.MORNING_END, ExamScheduleService.AFTERNOON_END);
            return false;
        }

        // Check max 4 exams per day per class
        long dailyExams = examSchedule.stream()
                .filter(s -> s.getClassId().equals(clazz.getClassId()) && s.getExamDate().equals(examDate))
                .count();
        if (dailyExams >= 4) {
            log.debug("Max 4 exams/day exceeded: classId={}, date={}, dailyExams={}",
                    clazz.getClassId(), examDate, dailyExams);
            return false;
        }

        // Check conflicts
        long sameTimeExams = examSchedule.stream()
                .filter(s -> s.getClassId().equals(clazz.getClassId()) &&
                        s.getExamDate().equals(examDate) &&
                        s.getExamTime().equals(examTime))
                .count();
        boolean classSlotFree = classSlots.getOrDefault(classKey, new HashSet<>()).isEmpty();
        boolean teacherSlotFree = teacherSlots.getOrDefault(teacherKey, new HashSet<>()).isEmpty();
        boolean roomSlotFree = roomSlots.getOrDefault(roomKey, new HashSet<>()).isEmpty();

        if (sameTimeExams > 0 || !classSlotFree || !teacherSlotFree || !roomSlotFree) {
            log.debug("Conflict detected: classId={}, subjectId={}, date={}, time={}, roomId={}, sameTimeExams={}, classSlotFree={}, teacherSlotFree={}, roomSlotFree={}",
                    clazz.getClassId(), subject.getId(), examDate, examTime, room.getRoomId(), sameTimeExams, classSlotFree, teacherSlotFree, roomSlotFree);
            return false;
        }

        return true;
    }

    private void addExamSlot(ClassResponse clazz, SubjectResponse subject, LocalDate examDate, LocalTime examTime,
                             List<TeacherResponse> teachers, List<TeacherSubjectClassResponse> teacherSubjects,
                             Long semesterId, Long schoolYearId) {
        ExamSchedule examSlot = new ExamSchedule();
        examSlot.setClassId(clazz.getClassId());
        examSlot.setSubjectId(subject.getId());
        examSlot.setExamDate(examDate);
        examSlot.setExamTime(examTime);
        examSlot.setRoomId(clazz.getRoomResponse().getRoomId());
        TeacherResponse teacher = getRandomTeacher(teachers);
        if (teacher == null) {
            log.error("No teacher available for exam slot: classId={}, subjectId={}", clazz.getClassId(), subject.getId());
            return;
        }
        examSlot.setTeacherId(teacher.getUserId());
        examSlot.setSemesterId(semesterId);
        examSlot.setSchoolYearId(schoolYearId);
        examSchedule.add(examSlot);
        updateSlotMaps(examSlot);
    }

    void updateSlotMaps(ExamSchedule slot) {
        if (slot.getTeacherId() == null || slot.getRoomId() == null || slot.getClassId() == null) {
            log.warn("Invalid slot data: teacherId={}, roomId={}, classId={}", slot.getTeacherId(), slot.getRoomId(), slot.getClassId());
            return;
        }
        String teacherKey = slot.getTeacherId() + "_" + slot.getExamDate() + "_" + slot.getExamTime().toString();
        String roomKey = slot.getRoomId() + "_" + slot.getExamDate() + "_" + slot.getExamTime().toString();
        String classKey = slot.getClassId() + "_" + slot.getExamDate() + "_" + slot.getExamTime().toString();

        String slotId = slot.getId() != null ? slot.getId().toString() : String.valueOf(examSchedule.indexOf(slot));

        teacherSlots.computeIfAbsent(teacherKey, k -> new HashSet<>()).add(slotId);
        roomSlots.computeIfAbsent(roomKey, k -> new HashSet<>()).add(slotId);
        classSlots.computeIfAbsent(classKey, k -> new HashSet<>()).add(slotId);
    }

    void removeFromSlotMaps(ExamSchedule slot) {
        if (slot.getTeacherId() == null || slot.getRoomId() == null || slot.getClassId() == null) {
            log.warn("Invalid slot data for removal: teacherId={}, roomId={}, classId={}", slot.getTeacherId(), slot.getRoomId(), slot.getClassId());
            return;
        }
        String teacherKey = slot.getTeacherId() + "_" + slot.getExamDate() + "_" + slot.getExamTime().toString();
        String roomKey = slot.getRoomId() + "_" + slot.getExamDate() + "_" + slot.getExamTime().toString();
        String classKey = slot.getClassId() + "_" + slot.getExamDate() + "_" + slot.getExamTime().toString();

        String slotId = slot.getId() != null ? slot.getId().toString() : String.valueOf(examSchedule.indexOf(slot));

        teacherSlots.getOrDefault(teacherKey, new HashSet<>()).remove(slotId);
        roomSlots.getOrDefault(roomKey, new HashSet<>()).remove(slotId);
        classSlots.getOrDefault(classKey, new HashSet<>()).remove(slotId);
    }

    private TeacherResponse getTeacherForSubject(SubjectResponse subject, ClassResponse clazz, List<TeacherResponse> teachers,
                                                 List<TeacherSubjectClassResponse> teacherSubjects) {
        return getRandomTeacher(teachers); // Always select random teacher for invigilation
    }

    private TeacherResponse getRandomTeacher(List<TeacherResponse> teachers) {
        if (teachers == null || teachers.isEmpty()) {
            log.error("No teachers available for random selection");
            return null;
        }
        Random rand = new Random();
        return teachers.get(rand.nextInt(teachers.size()));
    }

    public void calculateFitness(List<ClassResponse> classes, List<TeacherResponse> teachers,
                                 Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects,
                                 List<RoomResponse> rooms, List<SubjectExamInfo> subjectExamInfos) {
        fitness = 0;
        if (classToTeacherSubjects == null) {
            log.error("classToTeacherSubjects is null in calculateFitness");
            return;
        }

        int teacherConflictPenalty = 0;
        int roomConflictPenalty = 0;
        int missingExamPenalty = 0;
        int excessExamPenalty = 0;
        int dailyLoadPenalty = 0;
        int sameSubjectTimePenalty = 0;

        // Teacher conflicts
        for (Map.Entry<String, Set<String>> entry : teacherSlots.entrySet()) {
            int count = entry.getValue().size();
            if (count > 1) {
                teacherConflictPenalty += 200 * (count - 1);
            }
        }

        // Room conflicts
        for (Map.Entry<String, Set<String>> entry : roomSlots.entrySet()) {
            int count = entry.getValue().size();
            if (count > 1) {
                roomConflictPenalty += 200 * (count - 1);
            }
        }

        // Same subject time check
        Map<Long, List<ExamSchedule>> subjectExams = examSchedule.stream()
                .collect(Collectors.groupingBy(ExamSchedule::getSubjectId));
        for (Map.Entry<Long, List<ExamSchedule>> entry : subjectExams.entrySet()) {
            List<ExamSchedule> slots = entry.getValue();
            Set<String> dateTimes = slots.stream()
                    .map(s -> s.getExamDate() + "_" + s.getExamTime())
                    .collect(Collectors.toSet());
            if (dateTimes.size() > 1) {
                sameSubjectTimePenalty += 300 * (dateTimes.size() - 1);
            }
        }

        // Class-specific checks
        Map<Long, Map<Long, List<ExamSchedule>>> classSubjectExams = examSchedule.stream()
                .collect(Collectors.groupingBy(ExamSchedule::getClassId, Collectors.groupingBy(ExamSchedule::getSubjectId)));
        for (ClassResponse clazz : classes) {
            List<TeacherSubjectClassResponse> teacherSubjects = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList());
            Set<SubjectResponse> classSubjects = teacherSubjects.stream()
                    .map(TeacherSubjectClassResponse::getSubject)
                    .filter(s -> !s.getName().equals("Class Activities") && !s.getName().equals("Flag Salute Session"))
                    .collect(Collectors.toSet());

            Map<Long, List<ExamSchedule>> subjectExamsForClass = classSubjectExams.getOrDefault(clazz.getClassId(), Collections.emptyMap());
            for (SubjectResponse subject : classSubjects) {
                List<ExamSchedule> exams = subjectExamsForClass.getOrDefault(subject.getId(), Collections.emptyList());
                long examCount = exams.size();
                if (examCount < 1) {
                    missingExamPenalty += 100;
                    log.debug("Missing exam: classId={}, subjectId={}", clazz.getClassId(), subject.getId());
                } else if (examCount > 1) {
                    excessExamPenalty += (int) ((examCount - 1) * 100);
                }
            }

            // Penalize high daily exam loads
            Map<LocalDate, Long> dailyCounts = examSchedule.stream()
                    .filter(s -> s.getClassId().equals(clazz.getClassId()))
                    .collect(Collectors.groupingBy(ExamSchedule::getExamDate, Collectors.counting()));
            for (Long count : dailyCounts.values()) {
                if (count > 2) {
                    dailyLoadPenalty += (count - 2) * 50;
                }
            }
        }

        fitness = teacherConflictPenalty + roomConflictPenalty + missingExamPenalty + excessExamPenalty +
                dailyLoadPenalty + sameSubjectTimePenalty;

        log.debug("Fitness breakdown: teacherConflicts={}, roomConflicts={}, missingExams={}, excessExams={}, dailyLoad={}, sameSubjectTime={}",
                teacherConflictPenalty, roomConflictPenalty, missingExamPenalty, excessExamPenalty, dailyLoadPenalty, sameSubjectTimePenalty);
    }

    public void repairConflicts(List<ClassResponse> classes, List<TeacherResponse> teachers,
                                Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects,
                                List<RoomResponse> rooms, Long semesterId, Long schoolYearId,
                                LocalDate startDate, LocalDate endDate, List<SubjectExamInfo> subjectExamInfos) {
        log.debug("Repairing conflicts in chromosome...");
        Random rand = new Random();

        // Verify room uniqueness
        Set<Long> roomIds = classes.stream()
                .map(c -> c.getRoomResponse().getRoomId())
                .collect(Collectors.toSet());
        if (roomIds.size() != classes.size()) {
            log.error("Duplicate room IDs detected: roomIds={}", roomIds);
        }

        // Repair same-class conflicts
        Map<String, List<ExamSchedule>> classTimeSlots = examSchedule.stream()
                .collect(Collectors.groupingBy(s -> s.getClassId() + "_" + s.getExamDate() + "_" + s.getExamTime()));
        for (Map.Entry<String, List<ExamSchedule>> entry : classTimeSlots.entrySet()) {
            List<ExamSchedule> slots = entry.getValue();
            if (slots.size() > 1) {
                log.debug("Same-class conflict detected: key={}, slots={}", entry.getKey(), slots.size());
                for (int i = 1; i < slots.size(); i++) {
                    ExamSchedule slot = slots.get(i);
                    ClassResponse clazz = classes.stream()
                            .filter(c -> c.getClassId().equals(slot.getClassId()))
                            .findFirst()
                            .orElse(null);
                    if (clazz == null) continue;
                    SubjectResponse subject = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList()).stream()
                            .map(TeacherSubjectClassResponse::getSubject)
                            .filter(s -> s.getId().equals(slot.getSubjectId()))
                            .findFirst()
                            .orElse(null);
                    if (subject == null) continue;
                    TeacherResponse teacher = teachers.stream()
                            .filter(t -> t.getUserId().equals(slot.getTeacherId()))
                            .findFirst()
                            .orElse(null);
                    if (teacher == null) continue;
                    SubjectExamInfo examInfo = subjectExamInfos.stream()
                            .filter(info -> info.getSubjectId().equals(subject.getId()))
                            .findFirst()
                            .orElse(null);
                    if (examInfo == null) continue;

                    removeFromSlotMaps(slot);
                    LocalDateTime newSlot = findBestSlot(slot, teacher, clazz.getRoomResponse(), classes, teachers, rooms,
                            classToTeacherSubjects, startDate, endDate, examInfo);
                    if (newSlot != null) {
                        slot.setExamDate(newSlot.toLocalDate());
                        slot.setExamTime(newSlot.toLocalTime());
                        updateSlotMaps(slot);
                        log.debug("Resolved same-class conflict for slot: classId={}, subjectId={}, newDate={}, newTime={}",
                                slot.getClassId(), slot.getSubjectId(), newSlot.toLocalDate(), newSlot.toLocalTime());
                    } else {
                        examSchedule.remove(slot);
                        log.warn("Removed unresolvable same-class conflict slot: classId={}, subjectId={}",
                                slot.getClassId(), slot.getSubjectId());
                    }
                }
            }
        }

        // Repair teacher conflicts
        for (TeacherResponse teacher : teachers) {
            List<ExamSchedule> teacherConflicts = new ArrayList<>();
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                for (LocalTime examTime : ExamScheduleService.EXAM_TIMES) {
                    String key = teacher.getUserId() + "_" + date + "_" + examTime;
                    if (teacherSlots.getOrDefault(key, new HashSet<>()).size() > 1) {
                        LocalDate finalDate = date;
                        LocalTime finalTime = examTime;
                        teacherConflicts.addAll(examSchedule.stream()
                                .filter(s -> s.getTeacherId().equals(teacher.getUserId()) &&
                                        s.getExamDate().equals(finalDate) &&
                                        s.getExamTime().equals(finalTime))
                                .collect(Collectors.toList()));
                    }
                }
            }
            repairTeacherConflicts(teacherConflicts, teacher, classes, teachers, classToTeacherSubjects, rooms,
                    semesterId, schoolYearId, startDate, endDate, subjectExamInfos);
        }

        // Repair room conflicts
        for (RoomResponse room : rooms) {
            List<ExamSchedule> roomConflicts = new ArrayList<>();
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                for (LocalTime examTime : ExamScheduleService.EXAM_TIMES) {
                    String key = room.getRoomId() + "_" + date + "_" + examTime;
                    if (roomSlots.getOrDefault(key, new HashSet<>()).size() > 1) {
                        LocalDate finalDate = date;
                        LocalTime finalTime = examTime;
                        roomConflicts.addAll(examSchedule.stream()
                                .filter(s -> s.getRoomId().equals(room.getRoomId()) &&
                                        s.getExamDate().equals(finalDate) &&
                                        s.getExamTime().equals(finalTime))
                                .collect(Collectors.toList()));
                    }
                }
            }
            repairRoomConflicts(roomConflicts, room, classes, teachers, classToTeacherSubjects, rooms,
                    semesterId, schoolYearId, startDate, endDate, subjectExamInfos);
        }

        // Repair same subject time conflicts
        Map<Long, List<ExamSchedule>> subjectExams = examSchedule.stream()
                .collect(Collectors.groupingBy(ExamSchedule::getSubjectId));
        for (Map.Entry<Long, List<ExamSchedule>> entry : subjectExams.entrySet()) {
            Long subjectId = entry.getKey();
            List<ExamSchedule> slots = entry.getValue();
            Set<String> dateTimes = slots.stream()
                    .map(s -> s.getExamDate() + "_" + s.getExamTime())
                    .collect(Collectors.toSet());
            if (dateTimes.size() > 1) {
                ExamSchedule referenceSlot = slots.get(0);
                for (int i = 1; i < slots.size(); i++) {
                    ExamSchedule slot = slots.get(i);
                    removeFromSlotMaps(slot);
                    slot.setExamDate(referenceSlot.getExamDate());
                    slot.setExamTime(referenceSlot.getExamTime());
                    updateSlotMaps(slot);
                }
            }
        }

        // Repair missing and excess exams
        for (ClassResponse clazz : classes) {
            List<TeacherSubjectClassResponse> teacherSubjects = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList());
            Map<Long, Long> assignedCounts = examSchedule.stream()
                    .filter(s -> s.getClassId().equals(clazz.getClassId()))
                    .collect(Collectors.groupingBy(ExamSchedule::getSubjectId, Collectors.counting()));
            for (TeacherSubjectClassResponse ts : teacherSubjects) {
                SubjectResponse subject = ts.getSubject();
                if (subject.getName().equals("Class Activities") || subject.getName().equals("Flag Salute Session")) continue;
                SubjectExamInfo examInfo = subjectExamInfos.stream()
                        .filter(info -> info.getSubjectId().equals(subject.getId()))
                        .findFirst()
                        .orElse(null);
                if (examInfo == null) {
                    log.warn("No exam info for subject {} in class {}", subject.getId(), clazz.getClassId());
                    continue;
                }

                long assigned = assignedCounts.getOrDefault(subject.getId(), 0L);
                if (assigned < 1) {
                    TeacherResponse teacher = getRandomTeacher(teachers); // Use random teacher
                    List<ExamSchedule> sameSubjectSlots = examSchedule.stream()
                            .filter(s -> s.getSubjectId().equals(subject.getId()))
                            .collect(Collectors.toList());
                    if (!sameSubjectSlots.isEmpty()) {
                        ExamSchedule refSlot = sameSubjectSlots.get(0);
                        if (isSlotValid(clazz, subject, refSlot.getExamDate(), refSlot.getExamTime(), teacher, clazz.getRoomResponse(), examInfo)) {
                            addExamSlot(clazz, subject, refSlot.getExamDate(), refSlot.getExamTime(), teachers, teacherSubjects, semesterId, schoolYearId);
                            log.debug("Added missing exam for subject {} in class {} at existing slot", subject.getId(), clazz.getClassId());
                            continue;
                        }
                    }
                    List<LocalDate> examDates = new ArrayList<>();
                    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                        examDates.add(date);
                    }
                    Collections.shuffle(examDates, rand);
                    boolean assignedSlot = false;
                    for (LocalDate date : examDates) {
                        for (LocalTime examTime : ExamScheduleService.EXAM_TIMES) {
                            if (isSlotValid(clazz, subject, date, examTime, teacher, clazz.getRoomResponse(), examInfo)) {
                                addExamSlot(clazz, subject, date, examTime, teachers, teacherSubjects, semesterId, schoolYearId);
                                log.debug("Added missing exam for subject {} in class {} at new slot {} {}",
                                        subject.getId(), clazz.getClassId(), date, examTime);
                                assignedSlot = true;
                                break;
                            }
                        }
                        if (assignedSlot) break;
                    }
                    if (!assignedSlot) {
                        log.warn("Could not find valid slot for subject {} in class {}", subject.getId(), clazz.getClassId());
                    }
                } else if (assigned > 1) {
                    List<ExamSchedule> excessSlots = examSchedule.stream()
                            .filter(s -> s.getClassId().equals(clazz.getClassId()) && s.getSubjectId().equals(subject.getId()))
                            .collect(Collectors.toList());
                    for (int i = 1; i < excessSlots.size(); i++) {
                        removeFromSlotMaps(excessSlots.get(i));
                        examSchedule.remove(excessSlots.get(i));
                        log.debug("Removed excess exam for subject {} in class {}", subject.getId(), clazz.getClassId());
                    }
                }
            }
        }
    }

    private void repairTeacherConflicts(List<ExamSchedule> conflicts, TeacherResponse teacher, List<ClassResponse> classes,
                                        List<TeacherResponse> teachers, Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects,
                                        List<RoomResponse> rooms, Long semesterId, Long schoolYearId,
                                        LocalDate startDate, LocalDate endDate, List<SubjectExamInfo> subjectExamInfos) {
        log.debug("Repairing teacher conflicts for teacherId={}", teacher.getUserId());
        Random rand = new Random();

        for (int i = 1; i < conflicts.size(); i++) {
            ExamSchedule slot = conflicts.get(i);
            ClassResponse clazz = classes.stream()
                    .filter(c -> c.getClassId().equals(slot.getClassId()))
                    .findFirst()
                    .orElse(null);
            if (clazz == null) {
                log.warn("Class not found for slot: classId={}", slot.getClassId());
                continue;
            }
            SubjectResponse subject = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList()).stream()
                    .map(TeacherSubjectClassResponse::getSubject)
                    .filter(s -> s.getId().equals(slot.getSubjectId()))
                    .findFirst()
                    .orElse(null);
            if (subject == null) {
                log.warn("Subject not found for slot: subjectId={}", slot.getSubjectId());
                continue;
            }
            SubjectExamInfo examInfo = subjectExamInfos.stream()
                    .filter(info -> info.getSubjectId().equals(subject.getId()))
                    .findFirst()
                    .orElse(null);
            if (examInfo == null) {
                log.warn("Exam info not found for subjectId={}", subject.getId());
                continue;
            }

            // Try to align with existing same-subject slots
            List<ExamSchedule> sameSubjectSlots = examSchedule.stream()
                    .filter(s -> s.getSubjectId().equals(slot.getSubjectId()) && !s.equals(slot))
                    .collect(Collectors.toList());
            if (!sameSubjectSlots.isEmpty()) {
                ExamSchedule refSlot = sameSubjectSlots.get(0);
                TeacherResponse newTeacher = getRandomTeacher(teachers);
                if (newTeacher != null && isSlotValid(clazz, subject, refSlot.getExamDate(), refSlot.getExamTime(),
                        newTeacher, clazz.getRoomResponse(), examInfo)) {
                    removeFromSlotMaps(slot);
                    slot.setExamDate(refSlot.getExamDate());
                    slot.setExamTime(refSlot.getExamTime());
                    slot.setTeacherId(newTeacher.getUserId());
                    slot.setRoomId(clazz.getRoomResponse().getRoomId());
                    updateSlotMaps(slot);
                    log.debug("Reassigned slot to existing subject time: classId={}, subjectId={}, date={}, time={}, teacherId={}",
                            slot.getClassId(), slot.getSubjectId(), slot.getExamDate(), slot.getExamTime(), newTeacher.getUserId());
                    continue;
                }
            }

            // Find a new slot with a new random teacher
            removeFromSlotMaps(slot);
            TeacherResponse newTeacher = getRandomTeacher(teachers);
            if (newTeacher == null) {
                log.warn("No teacher available for reassignment: classId={}, subjectId={}", slot.getClassId(), slot.getSubjectId());
                updateSlotMaps(slot); // Revert
                continue;
            }
            LocalDateTime newSlotTime = findBestSlot(slot, newTeacher, clazz.getRoomResponse(), classes, teachers, rooms,
                    classToTeacherSubjects, startDate, endDate, examInfo);
            if (newSlotTime != null) {
                slot.setExamDate(newSlotTime.toLocalDate());
                slot.setExamTime(newSlotTime.toLocalTime());
                slot.setTeacherId(newTeacher.getUserId());
                slot.setRoomId(clazz.getRoomResponse().getRoomId());
                updateSlotMaps(slot);
                log.debug("Reassigned slot: classId={}, subjectId={}, date={}, time={}, teacherId={}",
                        slot.getClassId(), slot.getSubjectId(), slot.getExamDate(), slot.getExamTime(), newTeacher.getUserId());
            } else {
                log.warn("Could not find new slot, reverting: classId={}, subjectId={}", slot.getClassId(), slot.getSubjectId());
                updateSlotMaps(slot); // Revert instead of removing
            }
        }
    }

    private void repairRoomConflicts(List<ExamSchedule> conflicts, RoomResponse room, List<ClassResponse> classes,
                                     List<TeacherResponse> teachers, Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects,
                                     List<RoomResponse> rooms, Long semesterId, Long schoolYearId,
                                     LocalDate startDate, LocalDate endDate, List<SubjectExamInfo> subjectExamInfos) {
        log.debug("Repairing room conflicts for roomId={}", room.getRoomId());
        Random rand = new Random();

        // Group conflicts by class to handle same-class conflicts
        Map<Long, List<ExamSchedule>> conflictsByClass = conflicts.stream()
                .collect(Collectors.groupingBy(ExamSchedule::getClassId));

        for (List<ExamSchedule> classConflicts : conflictsByClass.values()) {
            if (classConflicts.size() <= 1) continue; // No conflict within this class

            log.debug("Same-class room conflict detected: classId={}, conflicts={}", classConflicts.get(0).getClassId(), classConflicts.size());

            // Keep the first slot, reassign others
            for (int i = 1; i < classConflicts.size(); i++) {
                ExamSchedule slot = classConflicts.get(i);
                ClassResponse clazz = classes.stream()
                        .filter(c -> c.getClassId().equals(slot.getClassId()))
                        .findFirst()
                        .orElse(null);
                if (clazz == null) {
                    log.warn("Class not found for slot: classId={}", slot.getClassId());
                    continue;
                }
                SubjectResponse subject = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList()).stream()
                        .map(TeacherSubjectClassResponse::getSubject)
                        .filter(s -> s.getId().equals(slot.getSubjectId()))
                        .findFirst()
                        .orElse(null);
                if (subject == null) {
                    log.warn("Subject not found for slot: subjectId={}", slot.getSubjectId());
                    continue;
                }
                SubjectExamInfo examInfo = subjectExamInfos.stream()
                        .filter(info -> info.getSubjectId().equals(subject.getId()))
                        .findFirst()
                        .orElse(null);
                if (examInfo == null) {
                    log.warn("Exam info not found for subjectId={}", subject.getId());
                    continue;
                }

                // Try to align with existing same-subject slots
                List<ExamSchedule> sameSubjectSlots = examSchedule.stream()
                        .filter(s -> s.getSubjectId().equals(slot.getSubjectId()) && !s.equals(slot))
                        .collect(Collectors.toList());
                if (!sameSubjectSlots.isEmpty()) {
                    ExamSchedule refSlot = sameSubjectSlots.get(0);
                    TeacherResponse newTeacher = getRandomTeacher(teachers);
                    if (newTeacher != null && isSlotValid(clazz, subject, refSlot.getExamDate(), refSlot.getExamTime(),
                            newTeacher, clazz.getRoomResponse(), examInfo)) {
                        removeFromSlotMaps(slot);
                        slot.setExamDate(refSlot.getExamDate());
                        slot.setExamTime(refSlot.getExamTime());
                        slot.setTeacherId(newTeacher.getUserId());
                        slot.setRoomId(clazz.getRoomResponse().getRoomId());
                        updateSlotMaps(slot);
                        log.debug("Reassigned slot to existing subject time: classId={}, subjectId={}, date={}, time={}, teacherId={}",
                                slot.getClassId(), slot.getSubjectId(), slot.getExamDate(), slot.getExamTime(), newTeacher.getUserId());
                        continue;
                    }
                }

                // Find a new slot
                removeFromSlotMaps(slot);
                TeacherResponse newTeacher = getRandomTeacher(teachers);
                if (newTeacher == null) {
                    log.warn("No teacher available for reassignment: classId={}, subjectId={}", slot.getClassId(), slot.getSubjectId());
                    updateSlotMaps(slot); // Revert
                    continue;
                }
                LocalDateTime newSlotTime = findBestSlot(slot, newTeacher, clazz.getRoomResponse(), classes, teachers, rooms,
                        classToTeacherSubjects, startDate, endDate, examInfo);
                if (newSlotTime != null) {
                    slot.setExamDate(newSlotTime.toLocalDate());
                    slot.setExamTime(newSlotTime.toLocalTime());
                    slot.setTeacherId(newTeacher.getUserId());
                    slot.setRoomId(clazz.getRoomResponse().getRoomId());
                    updateSlotMaps(slot);
                    log.debug("Reassigned slot: classId={}, subjectId={}, date={}, time={}, teacherId={}",
                            slot.getClassId(), slot.getSubjectId(), slot.getExamDate(), slot.getExamTime(), newTeacher.getUserId());
                } else {
                    log.warn("Could not find new slot, reverting: classId={}, subjectId={}", slot.getClassId(), slot.getSubjectId());
                    updateSlotMaps(slot); // Revert instead of removing
                }
            }
        }
    }

    private LocalDateTime findBestSlot(ExamSchedule slot, TeacherResponse teacher, RoomResponse room, List<ClassResponse> classes,
                                       List<TeacherResponse> teachers, List<RoomResponse> rooms,
                                       Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects,
                                       LocalDate startDate, LocalDate endDate, SubjectExamInfo examInfo) {
        Random rand = new Random();
        ClassResponse clazz = classes.stream()
                .filter(c -> c.getClassId().equals(slot.getClassId()))
                .findFirst()
                .orElse(null);
        if (clazz == null) return null;
        SubjectResponse subject = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList())
                .stream()
                .map(TeacherSubjectClassResponse::getSubject)
                .filter(s -> s.getId().equals(slot.getSubjectId()))
                .findFirst()
                .orElse(null);
        if (subject == null) return null;
        List<LocalDate> examDates = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            examDates.add(date);
        }
        Collections.shuffle(examDates, rand);
        for (LocalDate examDate : examDates) {
            for (LocalTime examTime : ExamScheduleService.EXAM_TIMES) {
                if (isSlotValid(clazz, subject, examDate, examTime, teacher, room, examInfo)) {
                    return LocalDateTime.of(examDate, examTime);
                }
            }
        }
        return null;
    }

    public void mutate(List<ClassResponse> classes, List<TeacherResponse> teachers,
                       Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects,
                       List<RoomResponse> rooms, Long semesterId, Long schoolYearId,
                       LocalDate startDate, LocalDate endDate, List<SubjectExamInfo> subjectExamInfos) {
        Random rand = new Random();
        if (examSchedule.isEmpty()) {
            log.warn("Exam schedule is empty, cannot mutate");
            return;
        }

        int mutationCount = rand.nextInt(3) + 1;
        log.debug("Applying {} mutations", mutationCount);

        for (int m = 0; m < mutationCount; m++) {
            Map<Long, List<ExamSchedule>> subjectExams = examSchedule.stream()
                    .collect(Collectors.groupingBy(ExamSchedule::getSubjectId));
            List<Long> subjectIds = new ArrayList<>(subjectExams.keySet());
            if (subjectIds.isEmpty()) continue;

            int mutationType = rand.nextInt(2);
            switch (mutationType) {
                case 0: // Swap slots for same subject
                    Long subjectId = subjectIds.get(rand.nextInt(subjectIds.size()));
                    List<ExamSchedule> slots = subjectExams.get(subjectId);
                    if (slots.size() <= 1) continue;

                    ExamSchedule slot1 = slots.get(rand.nextInt(slots.size()));
                    SubjectResponse subject = classToTeacherSubjects.values().stream()
                            .flatMap(List::stream)
                            .map(TeacherSubjectClassResponse::getSubject)
                            .filter(s -> s.getId().equals(subjectId))
                            .findFirst()
                            .orElse(null);
                    if (subject == null) continue;
                    SubjectExamInfo examInfo = subjectExamInfos.stream()
                            .filter(info -> info.getSubjectId().equals(subjectId))
                            .findFirst()
                            .orElse(null);
                    if (examInfo == null) continue;

                    ClassResponse clazz1 = classes.stream()
                            .filter(c -> c.getClassId().equals(slot1.getClassId()))
                            .findFirst()
                            .orElse(null);
                    if (clazz1 == null) continue;

                    TeacherResponse teacher1 = teachers.stream()
                            .filter(t -> t.getUserId().equals(slot1.getTeacherId()))
                            .findFirst()
                            .orElse(null);
                    if (teacher1 == null) continue;

                    removeFromSlotMaps(slot1);
                    LocalDateTime newSlot = findBestSlot(slot1, teacher1, clazz1.getRoomResponse(), classes, teachers, rooms,
                            classToTeacherSubjects, startDate, endDate, examInfo);
                    if (newSlot != null) {
                        slot1.setExamDate(newSlot.toLocalDate());
                        slot1.setExamTime(newSlot.toLocalTime());
                        updateSlotMaps(slot1);
                    } else {
                        updateSlotMaps(slot1);
                    }
                    break;

                case 1: // Reassign all slots for a subject
                    subjectId = subjectIds.get(rand.nextInt(subjectIds.size()));
                    slots = subjectExams.get(subjectId);
                    if (slots.isEmpty()) continue;
                    subject = classToTeacherSubjects.values().stream()
                            .flatMap(List::stream)
                            .map(TeacherSubjectClassResponse::getSubject)
                            .filter(s -> s.getId().equals(subjectId))
                            .findFirst()
                            .orElse(null);
                    if (subject == null) continue;
                    examInfo = subjectExamInfos.stream()
                            .filter(info -> info.getSubjectId().equals(subjectId))
                            .findFirst()
                            .orElse(null);
                    if (examInfo == null) continue;

                    for (ExamSchedule slot : slots) {
                        ClassResponse clazz = classes.stream()
                                .filter(c -> c.getClassId().equals(slot.getClassId()))
                                .findFirst()
                                .orElse(null);
                        if (clazz == null) continue;
                        TeacherResponse teacher = teachers.stream()
                                .filter(t -> t.getUserId().equals(slot.getTeacherId()))
                                .findFirst()
                                .orElse(null);
                        if (teacher == null) continue;

                        removeFromSlotMaps(slot);
                    }

                    TeacherResponse teacher = teachers.stream()
                            .filter(t -> t.getUserId().equals(slots.get(0).getTeacherId()))
                            .findFirst()
                            .orElse(null);
                    if (teacher == null) continue;
                    RoomResponse room = rooms.stream()
                            .filter(r -> r.getRoomId().equals(slots.get(0).getRoomId()))
                            .findFirst()
                            .orElse(null);
                    if (room == null) continue;

                    LocalDateTime newCommonSlot = findBestSlot(slots.get(0), teacher, room, classes, teachers, rooms,
                            classToTeacherSubjects, startDate, endDate, examInfo);
                    if (newCommonSlot != null) {
                        for (ExamSchedule slot : slots) {
                            slot.setExamDate(newCommonSlot.toLocalDate());
                            slot.setExamTime(newCommonSlot.toLocalTime());
                            updateSlotMaps(slot);
                        }
                    } else {
                        for (ExamSchedule slot : slots) {
                            updateSlotMaps(slot);
                        }
                    }
                    break;
            }
        }

        repairConflicts(classes, teachers, classToTeacherSubjects, rooms,
                semesterId, schoolYearId, startDate, endDate, subjectExamInfos);
        calculateFitness(classes, teachers, classToTeacherSubjects, rooms, subjectExamInfos);
        log.debug("Post-mutation fitness: {}", fitness);
    }

    public void localSearch(List<ClassResponse> classes, List<TeacherResponse> teachers,
                            Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects,
                            List<RoomResponse> rooms, Long semesterId, Long schoolYearId,
                            LocalDate startDate, LocalDate endDate, List<SubjectExamInfo> subjectExamInfos) {
        if (examSchedule.isEmpty()) {
            log.warn("Exam schedule is empty, cannot perform local search");
            return;
        }

        double bestFitness = fitness;
        List<ExamSchedule> bestSlots = new ArrayList<>();
        List<LocalDateTime[]> bestChanges = new ArrayList<>();
        List<ExamSchedule> lastSlots = null;
        Long lastSubjectId = null;
        Random rand = new Random();

        int maxIterations = Math.min(examSchedule.size() * 10, 1000);
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            Map<Long, List<ExamSchedule>> subjectExams = examSchedule.stream()
                    .collect(Collectors.groupingBy(ExamSchedule::getSubjectId));
            List<Long> subjects = new ArrayList<>(subjectExams.keySet());
            if (subjects.isEmpty()) continue;
            Long subjectId = subjects.get(rand.nextInt(subjects.size()));
            List<ExamSchedule> slots = subjectExams.get(subjectId);
            if (slots == null || slots.isEmpty()) continue;

            SubjectResponse subject = classToTeacherSubjects.values().stream()
                    .flatMap(List::stream)
                    .map(TeacherSubjectClassResponse::getSubject)
                    .filter(s -> s.getId().equals(subjectId))
                    .findFirst()
                    .orElse(null);
            if (subject == null) continue;
            SubjectExamInfo examInfo = subjectExamInfos.stream()
                    .filter(info -> info.getSubjectId().equals(subjectId))
                    .findFirst()
                    .orElse(null);
            if (examInfo == null) continue;

            for (ExamSchedule slot : slots) {
                removeFromSlotMaps(slot);
            }

            ExamSchedule referenceSlot = slots.get(0);
            ClassResponse clazz = classes.stream()
                    .filter(c -> c.getClassId().equals(referenceSlot.getClassId()))
                    .findFirst()
                    .orElse(null);
            if (clazz == null) {
                for (ExamSchedule slot : slots) {
                    updateSlotMaps(slot);
                }
                continue;
            }
            TeacherResponse teacher = teachers.stream()
                    .filter(t -> t.getUserId().equals(referenceSlot.getTeacherId()))
                    .findFirst()
                    .orElse(null);
            if (teacher == null) {
                for (ExamSchedule slot : slots) {
                    updateSlotMaps(slot);
                }
                continue;
            }

            LocalDate oldDate = referenceSlot.getExamDate();
            LocalTime oldTime = referenceSlot.getExamTime();

            LocalDateTime newTime = findBestSlot(referenceSlot, teacher, clazz.getRoomResponse(), classes, teachers,
                    rooms, classToTeacherSubjects, startDate, endDate, examInfo);
            if (newTime != null) {
                for (ExamSchedule slot : slots) {
                    slot.setExamDate(newTime.toLocalDate());
                    slot.setExamTime(newTime.toLocalTime());
                    updateSlotMaps(slot);
                }
                calculateFitness(classes, teachers, classToTeacherSubjects, rooms, subjectExamInfos);

                if (fitness < bestFitness) {
                    bestFitness = fitness;
                    bestSlots.clear();
                    bestChanges.clear();
                    lastSlots = new ArrayList<>(slots);
                    lastSubjectId = subjectId;
                    for (ExamSchedule slot : slots) {
                        bestSlots.add(new ExamSchedule(slot));
                    }
                    bestChanges.add(new LocalDateTime[] {
                            LocalDateTime.of(oldDate, oldTime),
                            newTime
                    });
                }
            }

            for (ExamSchedule slot : slots) {
                slot.setExamDate(oldDate);
                slot.setExamTime(oldTime);
                updateSlotMaps(slot);
            }
        }

        if (!bestSlots.isEmpty() && lastSlots != null) {
            for (int i = 0; i < lastSlots.size(); i++) {
                ExamSchedule slot = lastSlots.get(i);
                ExamSchedule bestSlot = bestSlots.get(i);
                removeFromSlotMaps(slot);
                slot.setExamDate(bestSlot.getExamDate());
                slot.setExamTime(bestSlot.getExamTime());
                updateSlotMaps(slot);
            }
            repairConflicts(classes, teachers, classToTeacherSubjects, rooms,
                    semesterId, schoolYearId, startDate, endDate, subjectExamInfos);
            calculateFitness(classes, teachers, classToTeacherSubjects, rooms, subjectExamInfos);
            log.debug("Local search improved fitness to {}", fitness);
        }
    }
}