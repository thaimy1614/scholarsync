package com.datn.timetable_service.service;

import com.datn.timetable_service.dto.SchoolService.ClassResponse;
import com.datn.timetable_service.dto.SchoolService.RoomResponse;
import com.datn.timetable_service.dto.UserService.TeacherResponse;
import com.datn.timetable_service.dto.subject_service.SubjectResponse;
import com.datn.timetable_service.dto.subject_service.TeacherSubjectClassResponse;
import com.datn.timetable_service.model.Timetable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Data
@NoArgsConstructor
public class Chromosome {
    private List<Timetable> timetable = new ArrayList<>();
    private double fitness;
    private Map<String, Set<String>> teacherSlots = new HashMap<>();
    private Map<String, Set<String>> roomSlots = new HashMap<>();
    private Map<String, Set<String>> classSlots = new HashMap<>();

    public Chromosome(Chromosome chromosome) {
        this.timetable = chromosome.getTimetable().stream()
                .map(Timetable::new)
                .collect(Collectors.toList());
        this.fitness = chromosome.getFitness();
        this.teacherSlots = new HashMap<>(chromosome.getTeacherSlots());
        this.roomSlots = new HashMap<>(chromosome.getRoomSlots());
        this.classSlots = new HashMap<>(chromosome.getClassSlots());
    }

    public void addFixedSlots(ClassResponse clazz, List<SubjectResponse> subjects, List<TeacherResponse> teachers,
                              List<TeacherSubjectClassResponse> teacherSubjects, Long semesterId, Long schoolYearId,
                              int weekNumber, LocalDate weekStartDate) {
        log.debug("Adding fixed slots for class {}", clazz.getClassName());
        SubjectResponse sinhHoat = subjects.stream()
                .filter(s -> s.getName().equalsIgnoreCase("Class Activities"))
                .findFirst()
                .orElse(null);
        SubjectResponse chaoCo = subjects.stream()
                .filter(s -> s.getName().equalsIgnoreCase("Flag Salute Session"))
                .findFirst()
                .orElse(null);
        if (sinhHoat != null) {
                Timetable slot = new Timetable();
                slot.setClassId(clazz.getClassId());
                slot.setSubjectId(sinhHoat.getId());
                slot.setDayOfWeek(5);
                slot.setSlot(clazz.getMainSession().equals("MORNING") ? 5 : 10);
                slot.setRoomId(clazz.getRoomResponse().getRoomId());
                slot.setTeacherId(clazz.getTeacher().getUserId());
                slot.setWeek(weekNumber);
                slot.setSemesterId(semesterId);
                slot.setSchoolYearId(schoolYearId);
                slot.setDate(weekStartDate.plusDays(4)); // Day 5 is Friday
                slot.setFixed(true); // Mark as fixed
                timetable.add(slot);
                updateSlotMaps(slot);
        }

        if (chaoCo != null) {
                Timetable slot = new Timetable();
                slot.setClassId(clazz.getClassId());
                slot.setSubjectId(chaoCo.getId());
                slot.setDayOfWeek(1);
                slot.setSlot(clazz.getMainSession().equals("MORNING") ? 1 : 10);
                slot.setRoomId(clazz.getRoomResponse().getRoomId());
                slot.setTeacherId(clazz.getTeacher().getUserId());
                slot.setWeek(weekNumber);
                slot.setSemesterId(semesterId);
                slot.setSchoolYearId(schoolYearId);
                slot.setDate(weekStartDate); // Day 1 is Monday
                slot.setFixed(true); // Mark as fixed
                timetable.add(slot);
                updateSlotMaps(slot);
        }
    }

    public void fillRandomSlots(ClassResponse clazz, List<SubjectResponse> subjects, List<TeacherResponse> teachers,
                                List<TeacherSubjectClassResponse> teacherSubjects, Long semesterId, Long schoolYearId,
                                int weekNumber, LocalDate weekStartDate) {
        log.info("Filling slots for class {}", clazz.getClassName());
        Random rand = new Random();
        boolean isMorningClass = clazz.getMainSession().equals("MORNING");
        List<Integer> mainSubjectSlots = isMorningClass ? Arrays.asList(1, 2, 3, 4, 5) : Arrays.asList(6, 7, 8, 9, 10);
        List<Integer> secondarySubjectSlots = isMorningClass ? Arrays.asList(7, 8, 9, 10) : Arrays.asList(1, 2, 3, 4);

        Map<Integer, Set<Integer>> assignedSlotsPerDay = new HashMap<>();
        Map<Integer, Map<SubjectResponse, Integer>> subjectCountsPerDay = new HashMap<>();
        for (int day = 1; day <= 6; day++) {
            assignedSlotsPerDay.put(day, new HashSet<>());
            subjectCountsPerDay.put(day, new HashMap<>());
        }

        // Sort subjects by weeklySlots (descending) to prioritize high-demand subjects
        List<SubjectResponse> sortedSubjects = subjects.stream()
                .filter(s -> !s.getName().equals("Class Activities") && !s.getName().equals("Flag Salute Session"))
                .sorted(Comparator.comparingInt(SubjectResponse::getWeeklySlots).reversed())
                .collect(Collectors.toList());

        for (SubjectResponse subject : sortedSubjects) {
            int slotsToAssign = subject.getWeeklySlots();
            TeacherResponse teacher = getTeacherForSubject(subject, clazz, teachers, teacherSubjects);
            List<Integer> validSlots = subject.isMainSubject() ? mainSubjectSlots : secondarySubjectSlots;

            // Handle consecutive slots
            if (subject.isPreferConsecutive() && slotsToAssign >= 2) {
                int maxAttempts = 10; // Limit attempts to avoid infinite loops
                while (slotsToAssign >= 2 && maxAttempts > 0) {
                    boolean assigned = false;
                    List<Integer> days = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6));
                    Collections.shuffle(days, rand);
                    for (int day : days) {
                        int subjectCount = subjectCountsPerDay.get(day).getOrDefault(subject, 0);
                        if (subjectCount >= 2) continue;
                        for (int i = 0; i < validSlots.size() - 1; i++) {
                            int currentSlot = validSlots.get(i);
                            int nextSlot = validSlots.get(i + 1);
                            if (!assignedSlotsPerDay.get(day).contains(currentSlot) &&
                                    !assignedSlotsPerDay.get(day).contains(nextSlot) &&
                                    isSlotValid(clazz, subject, day, currentSlot, teacher, clazz.getRoomResponse()) &&
                                    isSlotValid(clazz, subject, day, nextSlot, teacher, clazz.getRoomResponse())) {
                                addTimetableSlot(clazz, subject, day, currentSlot, teachers, teacherSubjects, semesterId, schoolYearId, weekNumber, weekStartDate);
                                addTimetableSlot(clazz, subject, day, nextSlot, teachers, teacherSubjects, semesterId, schoolYearId, weekNumber, weekStartDate);
                                assignedSlotsPerDay.get(day).add(currentSlot);
                                assignedSlotsPerDay.get(day).add(nextSlot);
                                subjectCountsPerDay.get(day).put(subject, subjectCount + 2);
                                slotsToAssign -= 2;
                                assigned = true;
                                break;
                            }
                        }
                        if (assigned) break;
                    }
                    maxAttempts--;
                    if (!assigned) break;
                }
            }

            // Assign remaining slots
            int maxAttempts = 10;
            while (slotsToAssign > 0 && maxAttempts > 0) {
                boolean assigned = false;
                List<Integer> days = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6));
                Collections.shuffle(days, rand);
                for (int day : days) {
                    int subjectCount = subjectCountsPerDay.get(day).getOrDefault(subject, 0);
                    if (subjectCount >= 2) continue;
                    List<Integer> availableSlots = getAvailableConsecutiveSlots(assignedSlotsPerDay.get(day), validSlots);
                    Collections.shuffle(availableSlots, rand);
                    for (int slot : availableSlots) {
                        if (isSlotValid(clazz, subject, day, slot, teacher, clazz.getRoomResponse())) {
                            addTimetableSlot(clazz, subject, day, slot, teachers, teacherSubjects, semesterId, schoolYearId, weekNumber, weekStartDate);
                            assignedSlotsPerDay.get(day).add(slot);
                            subjectCountsPerDay.get(day).put(subject, subjectCount + 1);
                            slotsToAssign--;
                            assigned = true;
                            break;
                        }
                    }
                    if (assigned) break;
                }
                maxAttempts--;
                if (!assigned) {
                    log.warn("Failed to assign slot for class {}, subject {}. Remaining slots: {}", clazz.getClassName(), subject.getName(), slotsToAssign);
                    break;
                }
            }
        }

        fillEmptySlots(clazz, subjects, teachers, teacherSubjects, assignedSlotsPerDay, subjectCountsPerDay, semesterId, schoolYearId, weekNumber, weekStartDate);
    }

    private List<Integer> getAvailableConsecutiveSlots(Set<Integer> assignedSlots, List<Integer> validSlots) {
        List<Integer> availableSlots = new ArrayList<>(validSlots);
        availableSlots.removeAll(assignedSlots);

        List<Integer> preferredSlots = new ArrayList<>();
        for (int slot : availableSlots) {
            if (assignedSlots.contains(slot - 1) || assignedSlots.contains(slot + 1)) {
                preferredSlots.add(slot);
            }
        }
        return preferredSlots.isEmpty() ? availableSlots : preferredSlots;
    }

    private void fillEmptySlots(ClassResponse clazz, List<SubjectResponse> subjects, List<TeacherResponse> teachers,
                                List<TeacherSubjectClassResponse> teacherSubjects, Map<Integer, Set<Integer>> assignedSlotsPerDay,
                                Map<Integer, Map<SubjectResponse, Integer>> subjectCountsPerDay, Long semesterId, Long schoolYearId,
                                int weekNumber, LocalDate weekStartDate) {
        log.info("Filling empty slots for class {}", clazz.getClassName());
        Random rand = new Random();
        boolean isMorningClass = clazz.getMainSession().equals("MORNING");
        List<Integer> mainSubjectSlots = isMorningClass ? Arrays.asList(1, 2, 3, 4, 5) : Arrays.asList(6, 7, 8, 9, 10);
        List<Integer> secondarySubjectSlots = isMorningClass ? Arrays.asList(7, 8, 9, 10) : Arrays.asList(1, 2, 3, 4);

        Map<SubjectResponse, Integer> remainingSlots = new HashMap<>();
        for (SubjectResponse subject : subjects) {
            if (subject.getName().equals("Class Activities") || subject.getName().equals("Flag Salute Session"))
                continue;
            long assignedCount = timetable.stream()
                    .filter(t -> t.getClassId().equals(clazz.getClassId()) && t.getSubjectId().equals(subject.getId()))
                    .count();
            int remaining = subject.getWeeklySlots() - (int) assignedCount;
            if (remaining > 0) {
                remainingSlots.put(subject, remaining);
            }
        }

        for (int day = 1; day <= 6; day++) {
            Set<Integer> assignedSlots = assignedSlotsPerDay.get(day);
            List<Integer> availableSlots = new ArrayList<>();
            availableSlots.addAll(mainSubjectSlots);
            availableSlots.addAll(secondarySubjectSlots);
            availableSlots.removeAll(assignedSlots);
            Collections.sort(availableSlots);

            List<SubjectResponse> subjectList = new ArrayList<>(remainingSlots.keySet());
            Collections.shuffle(subjectList, rand);

            List<Integer> preferredSlots = getAvailableConsecutiveSlots(assignedSlots, availableSlots);
            for (Integer slot : preferredSlots) {
                for (SubjectResponse subject : subjectList) {
                    if (remainingSlots.get(subject) <= 0) continue;
                    int subjectCount = subjectCountsPerDay.get(day).getOrDefault(subject, 0);
                    if (subjectCount >= 2) continue;
                    TeacherResponse teacher = getTeacherForSubject(subject, clazz, teachers, teacherSubjects);
                    boolean isMainSubject = subject.isMainSubject();
                    List<Integer> validSlots = isMainSubject ? mainSubjectSlots : secondarySubjectSlots;
                    if (validSlots.contains(slot) &&
                            isSlotValid(clazz, subject, day, slot, teacher, clazz.getRoomResponse())) {
                        addTimetableSlot(clazz, subject, day, slot, teachers, teacherSubjects, semesterId, schoolYearId, weekNumber, weekStartDate);
                        assignedSlots.add(slot);
                        subjectCountsPerDay.get(day).put(subject, subjectCount + 1);
                        remainingSlots.put(subject, remainingSlots.get(subject) - 1);
                        break;
                    }
                }
            }
        }
    }

    boolean isSlotValid(ClassResponse clazz, SubjectResponse subject, int day, int slot, TeacherResponse teacher, RoomResponse room) {
        if (subject.getName().equals("Physical Education") && (slot == 5 || slot == 6)) return false;
        boolean isMorningClass = clazz.getMainSession().equals("MORNING");
        boolean isMainSubject = subject.isMainSubject();
        if (isMorningClass) {
            if (isMainSubject && (slot < 1 || slot > 5)) return false;
            if (!isMainSubject && (slot < 7 || slot > 10)) return false;
        } else {
            if (isMainSubject && (slot < 6 || slot > 10)) return false;
            if (!isMainSubject && (slot < 1 || slot > 4)) return false;
        }
        String classKey = clazz.getClassId() + "_" + day + "_" + slot;
        String teacherKey = teacher.getUserId() + "_" + day + "_" + slot;
        String roomKey = room.getRoomId() + "_" + day + "_" + slot;
        long sameSessionCount = timetable.stream()
                .filter(t -> t.getClassId().equals(clazz.getClassId()) && t.getDayOfWeek() == day &&
                        t.getSubjectId().equals(subject.getId()) &&
                        ((t.getSlot() <= 5 && slot <= 5) || (t.getSlot() >= 6 && slot >= 6)))
                .count();
        return sameSessionCount < subject.getMaxSlotsPerSession() &&
                classSlots.getOrDefault(classKey, new HashSet<>()).isEmpty() &&
                teacherSlots.getOrDefault(teacherKey, new HashSet<>()).isEmpty() &&
                roomSlots.getOrDefault(roomKey, new HashSet<>()).isEmpty();
    }

    private void addTimetableSlot(ClassResponse clazz, SubjectResponse subject, int day, int slot,
                                  List<TeacherResponse> teachers, List<TeacherSubjectClassResponse> teacherSubjects,
                                  Long semesterId, Long schoolYearId, int weekNumber, LocalDate weekStartDate) {
        Timetable timetableSlot = new Timetable();
        timetableSlot.setClassId(clazz.getClassId());
        timetableSlot.setSubjectId(subject.getId());
        timetableSlot.setDayOfWeek(day);
        timetableSlot.setSlot(slot);
        timetableSlot.setRoomId(clazz.getRoomResponse().getRoomId());
        timetableSlot.setTeacherId(getTeacherForSubject(subject, clazz, teachers, teacherSubjects).getUserId());
        timetableSlot.setWeek(weekNumber);
        timetableSlot.setSemesterId(semesterId);
        timetableSlot.setSchoolYearId(schoolYearId);
        timetableSlot.setDate(weekStartDate.plusDays(day - 1));
        timetable.add(timetableSlot);
        updateSlotMaps(timetableSlot);
    }

    void updateSlotMaps(Timetable slot) {
        String teacherKey = slot.getTeacherId() + "_" + slot.getDayOfWeek() + "_" + slot.getSlot();
        String roomKey = slot.getRoomId() + "_" + slot.getDayOfWeek() + "_" + slot.getSlot();
        String classKey = slot.getClassId() + "_" + slot.getDayOfWeek() + "_" + slot.getSlot();

        String slotId = slot.getId() != null ? slot.getId().toString() : String.valueOf(timetable.indexOf(slot));

        teacherSlots.computeIfAbsent(teacherKey, k -> new HashSet<>()).add(slotId);
        roomSlots.computeIfAbsent(roomKey, k -> new HashSet<>()).add(slotId);
        classSlots.computeIfAbsent(classKey, k -> new HashSet<>()).add(slotId);
    }

    void removeFromSlotMaps(Timetable slot) {
        String teacherKey = slot.getTeacherId() + "_" + slot.getDayOfWeek() + "_" + slot.getSlot();
        String roomKey = slot.getRoomId() + "_" + slot.getDayOfWeek() + "_" + slot.getSlot();
        String classKey = slot.getClassId() + "_" + slot.getDayOfWeek() + "_" + slot.getSlot();

        String slotId = slot.getId() != null ? slot.getId().toString() : String.valueOf(timetable.indexOf(slot));

        teacherSlots.getOrDefault(teacherKey, new HashSet<>()).remove(slotId);
        roomSlots.getOrDefault(roomKey, new HashSet<>()).remove(slotId);
        classSlots.getOrDefault(classKey, new HashSet<>()).remove(slotId);
    }

    private TeacherResponse getTeacherForSubject(SubjectResponse subject, ClassResponse clazz, List<TeacherResponse> teachers,
                                                 List<TeacherSubjectClassResponse> teacherSubjects) {
        if (teacherSubjects == null || teacherSubjects.isEmpty()) {
            log.warn("teacherSubjects is null or empty, returning default teacher for subject {} in class {}",
                    subject.getName(), clazz.getClassName());
            return teachers.get(0);
        }
        return teacherSubjects.stream()
                .filter(ts -> ts.getSubject().getId().equals(subject.getId()) && ts.getClazz().getClassId().equals(clazz.getClassId()))
                .map(TeacherSubjectClassResponse::getTeacher)
                .findFirst()
                .orElse(teachers.get(0));
    }

    public void calculateFitness(List<ClassResponse> classes, List<TeacherResponse> teachers,
                                 Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects, List<RoomResponse> rooms) {
        fitness = 0;
        if (classToTeacherSubjects == null) {
            log.error("classToTeacherSubjects is null in calculateFitness");
            return;
        }

        int teacherConflictPenalty = 0;
        int roomConflictPenalty = 0;
        int invalidSubjectPenalty = 0;
        int missingSlotPenalty = 0;
        int excessSlotPenalty = 0;
        int invalidSlotPenalty = 0;
        int nonConsecutivePenalty = 0;
        int similarSchedulePenalty = 0;
        int excessDailyPenalty = 0;

        // Log missing slots details
        Map<String, Integer> missingSlotsDetails = new HashMap<>();

        // Teacher conflicts
        for (Map.Entry<String, Set<String>> entry : teacherSlots.entrySet()) {
            int count = entry.getValue().size();
            if (count > 1) {
                teacherConflictPenalty += 200 * (count - 1);
            }
        }

        // Room conflicts
//        for (RoomResponse room : rooms) {
//            if (room.isSpecial()) {
//                for (Map.Entry<String, Set<String>> entry : roomSlots.entrySet()) {
//                    if (entry.getKey().startsWith(room.getRoomId().toString())) {
//                        int count = entry.getValue().size();
//                        if (count > 1) {
//                            roomConflictPenalty += 200 * (count - 1);
//                        }
//                    }
//                }
//            }
//        }

        // Class-specific checks
        Map<Long, Map<Long, List<Timetable>>> classSubjectSlots = timetable.stream()
                .collect(Collectors.groupingBy(Timetable::getClassId, Collectors.groupingBy(Timetable::getSubjectId)));
        for (ClassResponse clazz : classes) {
            boolean isMorningClass = clazz.getMainSession().equals("MORNING");
            List<TeacherSubjectClassResponse> teacherSubjects = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList());
            Set<SubjectResponse> classSubjects = teacherSubjects.stream()
                    .map(TeacherSubjectClassResponse::getSubject)
                    .collect(Collectors.toSet());

            Map<Long, List<Timetable>> subjectSlots = classSubjectSlots.getOrDefault(clazz.getClassId(), Collections.emptyMap());
            for (SubjectResponse subject : classSubjects) {
                List<Timetable> slots = subjectSlots.getOrDefault(subject.getId(), Collections.emptyList());
                long slotCount = slots.size();
                if (!classSubjects.contains(subject)) {
                    invalidSubjectPenalty += slotCount * 1000;
                } else {
                    if (slotCount < subject.getWeeklySlots()) {
                        int missing = (int) (subject.getWeeklySlots() - slotCount);
                        missingSlotPenalty += missing * 100;
                        String key = clazz.getClassName() + "_" + subject.getName();
                        missingSlotsDetails.put(key, missing);
                    } else if (slotCount > subject.getWeeklySlots()) {
                        excessSlotPenalty += (slotCount - subject.getWeeklySlots()) * 100;
                    }
                    if (subject.isPreferConsecutive()) {
                        long consecutiveCount = countConsecutiveSlots(clazz, subject);
                        int expectedConsecutive = subject.getWeeklySlots() / 2;
                        if (consecutiveCount < expectedConsecutive) {
                            nonConsecutivePenalty += (expectedConsecutive - (int) consecutiveCount) * 50;
                        }
                    }
                }
                long invalidSlots = slots.stream()
                        .filter(t -> {
                            boolean isMainSubject = subject.isMainSubject();
                            if (isMorningClass) {
                                return (isMainSubject && (t.getSlot() < 1 || t.getSlot() > 5)) ||
                                        (!isMainSubject && (t.getSlot() < 7 || t.getSlot() > 10));
                            } else {
                                return (isMainSubject && (t.getSlot() < 6 || t.getSlot() > 10)) ||
                                        (!isMainSubject && (t.getSlot() < 1 || t.getSlot() > 4));
                            }
                        })
                        .count();
                invalidSlotPenalty += invalidSlots * 500;
            }

            // Penalize similar schedules between paired days
            Map<Integer, Set<Long>> subjectsPerDay = timetable.stream()
                    .filter(t -> t.getClassId().equals(clazz.getClassId()))
                    .collect(Collectors.groupingBy(
                            Timetable::getDayOfWeek,
                            Collectors.mapping(Timetable::getSubjectId, Collectors.toSet())
                    ));
            int[][] dayPairs = {{1, 2}, {3, 4}, {5, 6}};
            for (int[] pair : dayPairs) {
                int day1 = pair[0];
                int day2 = pair[1];
                Set<Long> subjectsDay1 = subjectsPerDay.getOrDefault(day1, new HashSet<>());
                Set<Long> subjectsDay2 = subjectsPerDay.getOrDefault(day2, new HashSet<>());
                Set<Long> commonSubjects = new HashSet<>(subjectsDay1);
                commonSubjects.retainAll(subjectsDay2);
                if (commonSubjects.size() > 2) {
                    similarSchedulePenalty += (commonSubjects.size() - 2) * 50;
                }
            }

            Map<Integer, Long> dailyCounts = timetable.stream()
                    .filter(t -> t.getClassId().equals(clazz.getClassId()))
                    .collect(Collectors.groupingBy(Timetable::getDayOfWeek, Collectors.counting()));
            for (Long count : dailyCounts.values()) {
                if (count > 10) {
                    excessDailyPenalty += (int) ((count - 10) * 10);
                }
            }
        }

        fitness = teacherConflictPenalty + roomConflictPenalty + invalidSubjectPenalty +
                missingSlotPenalty + excessSlotPenalty + invalidSlotPenalty +
                nonConsecutivePenalty + similarSchedulePenalty + excessDailyPenalty;

        // Log missing slots details
        if (!missingSlotsDetails.isEmpty()) {
            log.info("Missing slots details: {}", missingSlotsDetails);
        }

        log.debug("Fitness breakdown: teacherConflicts={}, roomConflicts={}, invalidSubjects={}, " +
                        "missingSlots={}, excessSlots={}, invalidSlots={}, nonConsecutive={}, " +
                        "similarSchedules={}, excessDaily={}",
                teacherConflictPenalty, roomConflictPenalty, invalidSubjectPenalty,
                missingSlotPenalty, excessSlotPenalty, invalidSlotPenalty,
                nonConsecutivePenalty, similarSchedulePenalty, excessDailyPenalty);
    }

    public void repairConflicts(List<ClassResponse> classes, List<TeacherResponse> teachers,
                                Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects,
                                List<RoomResponse> rooms, Long semesterId, Long schoolYearId, int weekNumber, LocalDate weekStartDate) {
        log.debug("Repairing conflicts in chromosome...");
        Random rand = new Random();
        if (classToTeacherSubjects == null) {
            log.error("classToTeacherSubjects is null in repairConflicts");
            return;
        }

        // Step 1: Repair invalid slots
        for (ClassResponse clazz : classes) {
            boolean isMorningClass = clazz.getMainSession().equals("MORNING");
            List<Timetable> invalidSlots = timetable.stream()
                    .filter(t -> t.getClassId().equals(clazz.getClassId()) && !t.isFixed())
                    .filter(t -> {
                        SubjectResponse subject = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList())
                                .stream()
                                .map(TeacherSubjectClassResponse::getSubject)
                                .filter(s -> s.getId().equals(t.getSubjectId()))
                                .findFirst()
                                .orElse(null);
                        if (subject == null) return false;
                        boolean isMainSubject = subject.isMainSubject();
                        if (isMorningClass) {
                            return (isMainSubject && (t.getSlot() < 1 || t.getSlot() > 5)) ||
                                    (!isMainSubject && (t.getSlot() < 7 || t.getSlot() > 10));
                        } else {
                            return (isMainSubject && (t.getSlot() < 6 || t.getSlot() > 10)) ||
                                    (!isMainSubject && (t.getSlot() < 1 || t.getSlot() > 4));
                        }
                    })
                    .collect(Collectors.toList());
            for (Timetable slot : invalidSlots) {
                SubjectResponse subject = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList())
                        .stream()
                        .map(TeacherSubjectClassResponse::getSubject)
                        .filter(s -> s.getId().equals(slot.getSubjectId()))
                        .findFirst()
                        .orElse(null);
                if (subject == null || subject.getName().equals("Class Activities") || subject.getName().equals("Flag Salute Session"))
                    continue;
                TeacherResponse teacher = teachers.stream()
                        .filter(t -> t.getUserId().equals(slot.getTeacherId()))
                        .findFirst()
                        .orElse(null);
                if (teacher == null) continue;
                removeFromSlotMaps(slot);
                int[] newSlot = findBestSlotWithSwap(slot, teacher, clazz.getRoomResponse(), classes, teachers, rooms, classToTeacherSubjects);
                if (newSlot != null) {
                    slot.setDayOfWeek(newSlot[0]);
                    slot.setSlot(newSlot[1]);
                    slot.setDate(weekStartDate.plusDays(newSlot[0] - 1));
                    updateSlotMaps(slot);
                } else {
                    List<Timetable> otherSlots = timetable.stream()
                            .filter(t -> !t.isFixed())
                            .filter(t -> t.getClassId().equals(clazz.getClassId()) &&
                                    !classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList())
                                            .stream()
                                            .map(TeacherSubjectClassResponse::getSubject)
                                            .filter(s -> s.getId().equals(t.getSubjectId()))
                                            .findFirst()
                                            .map(s -> s.getName().equals("Class Activities") || s.getName().equals("Flag Salute Session"))
                                            .orElse(true))
                            .filter(t -> isSlotValid(clazz, subject, t.getDayOfWeek(), t.getSlot(), teacher, clazz.getRoomResponse()))
                            .collect(Collectors.toList());
                    if (!otherSlots.isEmpty()) {
                        Timetable swapSlot = otherSlots.get(rand.nextInt(otherSlots.size()));
                        removeFromSlotMaps(swapSlot);
                        int tempDay = swapSlot.getDayOfWeek();
                        int tempSlot = swapSlot.getSlot();
                        swapSlot.setDayOfWeek(slot.getDayOfWeek());
                        swapSlot.setSlot(slot.getSlot());
                        swapSlot.setDate(weekStartDate.plusDays(slot.getDayOfWeek() - 1));
                        slot.setDayOfWeek(tempDay);
                        slot.setSlot(tempSlot);
                        slot.setDate(weekStartDate.plusDays(tempDay - 1));
                        updateSlotMaps(swapSlot);
                        updateSlotMaps(slot);
                    } else {
                        timetable.remove(slot);
                    }
                }
            }
        }

        // Step 2: Repair teacher conflicts
        for (TeacherResponse teacher : teachers) {
            List<Timetable> teacherConflicts = new ArrayList<>();
            for (int day = 1; day <= 6; day++) {
                for (int slot = 1; slot <= 10; slot++) {
                    String key = teacher.getUserId() + "_" + day + "_" + slot;
                    if (teacherSlots.getOrDefault(key, new HashSet<>()).size() > 1) {
                        int finalDay = day;
                        int finalSlot = slot;
                        teacherConflicts.addAll(timetable.stream()
                                .filter(t -> t.getTeacherId().equals(teacher.getUserId()) &&
                                        t.getDayOfWeek() == finalDay &&
                                        t.getSlot() == finalSlot &&
                                        !t.isFixed()
                                )
                                .collect(Collectors.toList()));
                    }
                }
            }
            repairTeacherConflicts(teacherConflicts, teacher, classes, teachers, classToTeacherSubjects, rooms, semesterId, schoolYearId, weekNumber, weekStartDate);
        }

        for (ClassResponse clazz : classes) {
            List<TeacherSubjectClassResponse> teacherSubjects = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList());
            Map<Long, Long> assignedCounts = timetable.stream()
                    .filter(t -> t.getClassId().equals(clazz.getClassId()))
                    .collect(Collectors.groupingBy(Timetable::getSubjectId, Collectors.counting()));
            for (TeacherSubjectClassResponse ts : teacherSubjects) {
                SubjectResponse subject = ts.getSubject();
                long assigned = assignedCounts.getOrDefault(subject.getId(), 0L);
                int required = subject.getWeeklySlots();
                if (assigned < required) {
                    int slotsToAdd = (int) (required - assigned);
                    TeacherResponse teacher = getTeacherForSubject(subject, clazz, teachers, teacherSubjects);
                    if (teacher == null) {
                        log.error("No teacher available for subject {} in class {}", subject.getName(), clazz.getClassName());
                        continue;
                    }
                    boolean isMorningClass = clazz.getMainSession().equals("MORNING");
                    List<Integer> validSlots = subject.isMainSubject()
                            ? (isMorningClass ? Arrays.asList(1, 2, 3, 4, 5) : Arrays.asList(6, 7, 8, 9, 10))
                            : (isMorningClass ? Arrays.asList(7, 8, 9, 10) : Arrays.asList(1, 2, 3, 4));

                    // Prefer fixed slots for Class Activities and Flag Salute Session
                    if (subject.getName().equals("Class Activities")) {
                        int preferredDay = 5;
                        int preferredSlot = isMorningClass ? 5 : 10;
                        if (slotsToAdd > 0 && isSlotValid(clazz, subject, preferredDay, preferredSlot, teacher, clazz.getRoomResponse())) {
                            Timetable slot = new Timetable();
                            slot.setClassId(clazz.getClassId());
                            slot.setSubjectId(subject.getId());
                            slot.setDayOfWeek(preferredDay);
                            slot.setSlot(preferredSlot);
                            slot.setRoomId(clazz.getRoomResponse().getRoomId());
                            slot.setTeacherId(teacher.getUserId());
                            slot.setWeek(weekNumber);
                            slot.setSemesterId(semesterId);
                            slot.setSchoolYearId(schoolYearId);
                            slot.setDate(weekStartDate.plusDays(4));
                            slot.setFixed(true);
                            timetable.add(slot);
                            updateSlotMaps(slot);
                            slotsToAdd--;
                            log.debug("Repaired Class Activities slot for class {}, day {}, slot {}", clazz.getClassName(), preferredDay, preferredSlot);
                        }
                    } else if (subject.getName().equals("Flag Salute Session")) {
                        int preferredDay = 1;
                        int preferredSlot = isMorningClass ? 1 : 10;
                        if (slotsToAdd > 0 && isSlotValid(clazz, subject, preferredDay, preferredSlot, teacher, clazz.getRoomResponse())) {
                            Timetable slot = new Timetable();
                            slot.setClassId(clazz.getClassId());
                            slot.setSubjectId(subject.getId());
                            slot.setDayOfWeek(preferredDay);
                            slot.setSlot(preferredSlot);
                            slot.setRoomId(clazz.getRoomResponse().getRoomId());
                            slot.setTeacherId(teacher.getUserId());
                            slot.setWeek(weekNumber);
                            slot.setSemesterId(semesterId);
                            slot.setSchoolYearId(schoolYearId);
                            slot.setDate(weekStartDate);
                            slot.setFixed(true);
                            timetable.add(slot);
                            updateSlotMaps(slot);
                            slotsToAdd--;
                            log.debug("Repaired Flag Salute Session slot for class {}, day {}, slot {}", clazz.getClassName(), preferredDay, preferredSlot);
                        }
                    }

                    // Assign remaining slots
                    for (int day = 1; day <= 6 && slotsToAdd > 0; day++) {
                        for (int slot : validSlots) {
                            if (slotsToAdd <= 0) break;
                            if (isSlotValid(clazz, subject, day, slot, teacher, clazz.getRoomResponse())) {
                                Timetable slotObj = new Timetable();
                                slotObj.setClassId(clazz.getClassId());
                                slotObj.setSubjectId(subject.getId());
                                slotObj.setDayOfWeek(day);
                                slotObj.setSlot(slot);
                                slotObj.setRoomId(clazz.getRoomResponse().getRoomId());
                                slotObj.setTeacherId(teacher.getUserId());
                                slotObj.setWeek(weekNumber);
                                slotObj.setSemesterId(semesterId);
                                slotObj.setSchoolYearId(schoolYearId);
                                slotObj.setDate(weekStartDate.plusDays(day - 1));
                                slotObj.setFixed(subject.getName().equals("Class Activities") || subject.getName().equals("Flag Salute Session"));
                                timetable.add(slotObj);
                                updateSlotMaps(slotObj);
                                slotsToAdd--;
                                log.debug("Added missing slot for class {}, subject {}, day {}, slot {}", clazz.getClassName(), subject.getName(), day, slot);
                            }
                        }
                    }

                    // Fallback: Relax constraints
                    if (slotsToAdd > 0) {
                        log.warn("Could not assign {} slots for class {}, subject {}. Relaxing constraints.", slotsToAdd, clazz.getClassName(), subject.getName());
                        for (int day = 1; day <= 6 && slotsToAdd > 0; day++) {
                            for (int slot = 1; slot <= 10; slot++) {
                                if (slotsToAdd <= 0) break;
                                String classKey = clazz.getClassId() + "_" + day + "_" + slot;
                                String teacherKey = teacher.getUserId() + "_" + day + "_" + slot;
                                String roomKey = clazz.getRoomResponse().getRoomId() + "_" + day + "_" + slot;
                                if (classSlots.getOrDefault(classKey, new HashSet<>()).isEmpty() &&
                                        teacherSlots.getOrDefault(teacherKey, new HashSet<>()).isEmpty() &&
                                        roomSlots.getOrDefault(roomKey, new HashSet<>()).isEmpty()) {
                                    Timetable slotObj = new Timetable();
                                    slotObj.setClassId(clazz.getClassId());
                                    slotObj.setSubjectId(subject.getId());
                                    slotObj.setDayOfWeek(day);
                                    slotObj.setSlot(slot);
                                    slotObj.setRoomId(clazz.getRoomResponse().getRoomId());
                                    slotObj.setTeacherId(teacher.getUserId());
                                    slotObj.setWeek(weekNumber);
                                    slotObj.setSemesterId(semesterId);
                                    slotObj.setSchoolYearId(schoolYearId);
                                    slotObj.setDate(weekStartDate.plusDays(day - 1));
                                    slotObj.setFixed(subject.getName().equals("Class Activities") || subject.getName().equals("Flag Salute Session"));
                                    timetable.add(slotObj);
                                    updateSlotMaps(slotObj);
                                    slotsToAdd--;
                                    log.debug("Added fallback slot for class {}, subject {}, day {}, slot {}", clazz.getClassName(), subject.getName(), day, slot);
                                }
                            }
                        }
                    }
                }
            }
        }

        //        for (RoomResponse room : rooms) {
//            if (room.isSpecial()) {
//                List<Timetable> roomConflicts = new ArrayList<>();
//                for (int day = 1; day <= 6; day++) {
//                    for (int slot = 1; slot <= 10; slot++) {
//                        String key = room.getRoomId() + "_" + day + "_" + slot;
//                        if (roomSlots.getOrDefault(key, new HashSet<>()).size() > 1) {
//                            int finalDay = day;
//                            int finalSlot = slot;
//                            roomConflicts.addAll(timetable.stream()
//                                    .filter(t -> t.getRoomId().equals(room.getRoomId()) &&
//                                            t.getDayOfWeek() == finalDay &&
//                                            t.getSlot() == finalSlot)
//                                    .collect(Collectors.toList()));
//                        }
//                    }
//                }
//                repairRoomConflicts(roomConflicts, room, classes, teachers, classToTeacherSubjects, rooms, semesterId, schoolYearId, weekNumber, weekStartDate);
//            }
//        }

        // Step 4: Repair consecutive slots
        for (ClassResponse clazz : classes) {
            List<TeacherSubjectClassResponse> teacherSubjects = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList());
            List<SubjectResponse> classSubjects = teacherSubjects.stream()
                    .map(TeacherSubjectClassResponse::getSubject)
                    .distinct()
                    .collect(Collectors.toList());
            for (SubjectResponse subject : classSubjects) {
                if (subject.isPreferConsecutive()) {
                    List<Timetable> slots = timetable.stream()
                            .filter(t -> t.getClassId().equals(clazz.getClassId()) &&
                                    t.getSubjectId().equals(subject.getId()) &&
                                    !t.isFixed()
                            )
                            .collect(Collectors.toList());
                    int expectedConsecutive = subject.getWeeklySlots() / 2;
                    long consecutiveCount = countConsecutiveSlots(clazz, subject);
                    if (consecutiveCount < expectedConsecutive) {
                        repairConsecutiveSlots(slots, clazz, subject, expectedConsecutive - (int) consecutiveCount,
                                teachers, teacherSubjects, rooms, semesterId, schoolYearId, weekNumber, weekStartDate, classToTeacherSubjects);
                    }
                }
            }
        }

        // Step 5: Repair excess daily slots
        for (ClassResponse clazz : classes) {
            for (int day = 1; day <= 6; day++) {
                int finalDay = day;
                List<Timetable> dailySlots = timetable.stream()
                        .filter(t -> t.getClassId().equals(clazz.getClassId()) &&
                                t.getDayOfWeek() == finalDay &&
                                !t.isFixed())
                        .collect(Collectors.toList());
                if (dailySlots.size() > 10) {
                    for (int i = 10; i < dailySlots.size(); i++) {
                        Timetable slot = dailySlots.get(i);
                        SubjectResponse subject = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList())
                                .stream()
                                .map(TeacherSubjectClassResponse::getSubject)
                                .filter(s -> s.getId().equals(slot.getSubjectId()))
                                .findFirst()
                                .orElse(null);
                        if (subject == null || subject.getName().equals("Class Activities") || subject.getName().equals("Flag Salute Session"))
                            continue;
                        TeacherResponse teacher = teachers.stream()
                                .filter(t -> t.getUserId().equals(slot.getTeacherId()))
                                .findFirst()
                                .orElse(null);
                        if (teacher == null) continue;
                        removeFromSlotMaps(slot);
                        int[] newSlot = findBestSlotWithSwap(slot, teacher, clazz.getRoomResponse(), classes, teachers, rooms, classToTeacherSubjects);
                        if (newSlot != null) {
                            slot.setDayOfWeek(newSlot[0]);
                            slot.setSlot(newSlot[1]);
                            slot.setDate(weekStartDate.plusDays(newSlot[0] - 1));
                            updateSlotMaps(slot);
                        } else {
                            timetable.remove(slot);
                        }
                    }
                }
            }
        }
    }

    private void repairTeacherConflicts(List<Timetable> conflicts, TeacherResponse teacher, List<ClassResponse> classes,
                                        List<TeacherResponse> teachers, Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects,
                                        List<RoomResponse> rooms, Long semesterId, Long schoolYearId, int weekNumber, LocalDate weekStartDate) {
        Random rand = new Random();
        for (int i = 1; i < conflicts.size(); i++) {
            Timetable slot = conflicts.get(i);
            SubjectResponse subject = classToTeacherSubjects.values().stream()
                    .flatMap(List::stream)
                    .map(TeacherSubjectClassResponse::getSubject)
                    .filter(s -> s.getId().equals(slot.getSubjectId()))
                    .findFirst()
                    .orElse(null);
            if (subject == null || subject.getName().equals("Class Activities") || subject.getName().equals("Flag Salute Session"))
                continue;
            ClassResponse clazz = classes.stream()
                    .filter(c -> c.getClassId().equals(slot.getClassId()))
                    .findFirst()
                    .orElse(null);
            if (clazz == null) continue;
            removeFromSlotMaps(slot);
            int[] newSlot = findBestSlotWithSwap(slot, teacher, clazz.getRoomResponse(), classes, teachers, rooms, classToTeacherSubjects);
            if (newSlot != null) {
                slot.setDayOfWeek(newSlot[0]);
                slot.setSlot(newSlot[1]);
                slot.setDate(weekStartDate.plusDays(newSlot[0] - 1));
                updateSlotMaps(slot);
                log.debug("Repaired teacher conflict for subject {} to day {}, slot {}", subject.getName(), newSlot[0], newSlot[1]);
            } else {
                List<Timetable> otherSlots = timetable.stream()
                        .filter(t -> {
                            SubjectResponse s = classToTeacherSubjects.values().stream()
                                    .flatMap(List::stream)
                                    .map(TeacherSubjectClassResponse::getSubject)
                                    .filter(sub -> sub.getId().equals(t.getSubjectId()))
                                    .findFirst()
                                    .orElse(null);
                            return s != null && !s.getName().equals("Class Activities") && !s.getName().equals("Flag Salute Session");
                        })
                        .filter(t -> isSlotValid(clazz, subject, t.getDayOfWeek(), t.getSlot(), teacher, clazz.getRoomResponse()))
                        .collect(Collectors.toList());
                if (!otherSlots.isEmpty()) {
                    Timetable swapSlot = otherSlots.get(rand.nextInt(otherSlots.size()));
                    removeFromSlotMaps(swapSlot);
                    int tempDay = swapSlot.getDayOfWeek();
                    int tempSlot = swapSlot.getSlot();
                    swapSlot.setDayOfWeek(slot.getDayOfWeek());
                    swapSlot.setSlot(slot.getSlot());
                    swapSlot.setDate(weekStartDate.plusDays(slot.getDayOfWeek() - 1));
                    slot.setDayOfWeek(tempDay);
                    slot.setSlot(tempSlot);
                    slot.setDate(weekStartDate.plusDays(tempDay - 1));
                    updateSlotMaps(swapSlot);
                    updateSlotMaps(slot);
                } else {
                    timetable.remove(slot);
                }
            }
        }
    }

    private void repairRoomConflicts(List<Timetable> conflicts, RoomResponse room, List<ClassResponse> classes,
                                     List<TeacherResponse> teachers, Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects,
                                     List<RoomResponse> rooms, Long semesterId, Long schoolYearId, int weekNumber, LocalDate weekStartDate) {
        Random rand = new Random();
        for (int i = 1; i < conflicts.size(); i++) {
            Timetable slot = conflicts.get(i);
            SubjectResponse subject = classToTeacherSubjects.values().stream()
                    .flatMap(List::stream)
                    .map(TeacherSubjectClassResponse::getSubject)
                    .filter(s -> s.getId().equals(slot.getSubjectId()))
                    .findFirst()
                    .orElse(null);
            if (subject == null) continue;
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
            int[] newSlot = findBestSlotWithSwap(slot, teacher, room, classes, teachers, rooms, classToTeacherSubjects);
            if (newSlot != null) {
                slot.setDayOfWeek(newSlot[0]);
                slot.setSlot(newSlot[1]);
                slot.setDate(weekStartDate.plusDays(newSlot[0] - 1));
                updateSlotMaps(slot);
            } else {
                List<Timetable> otherSlots = timetable.stream()
                        .filter(t -> {
                            SubjectResponse s = classToTeacherSubjects.values().stream()
                                    .flatMap(List::stream)
                                    .map(TeacherSubjectClassResponse::getSubject)
                                    .filter(sub -> sub.getId().equals(t.getSubjectId()))
                                    .findFirst()
                                    .orElse(null);
                            return s != null && !s.getName().equals("Class Activities") && !s.getName().equals("Flag Salute Session");
                        })
                        .filter(t -> isSlotValid(clazz, subject, t.getDayOfWeek(), t.getSlot(), teacher, room))
                        .collect(Collectors.toList());
                if (!otherSlots.isEmpty()) {
                    Timetable swapSlot = otherSlots.get(rand.nextInt(otherSlots.size()));
                    removeFromSlotMaps(swapSlot);
                    int tempDay = swapSlot.getDayOfWeek();
                    int tempSlot = swapSlot.getSlot();
                    swapSlot.setDayOfWeek(slot.getDayOfWeek());
                    swapSlot.setSlot(slot.getSlot());
                    swapSlot.setDate(weekStartDate.plusDays(slot.getDayOfWeek() - 1));
                    slot.setDayOfWeek(tempDay);
                    slot.setSlot(tempSlot);
                    slot.setDate(weekStartDate.plusDays(tempDay - 1));
                    updateSlotMaps(swapSlot);
                    updateSlotMaps(slot);
                } else {
                    timetable.remove(slot);
                }
            }
        }
    }

    private void repairConsecutiveSlots(List<Timetable> slots, ClassResponse clazz, SubjectResponse subject, int pairsNeeded,
                                        List<TeacherResponse> teachers, List<TeacherSubjectClassResponse> teacherSubjects,
                                        List<RoomResponse> rooms, Long semesterId, Long schoolYearId, int weekNumber, LocalDate weekStartDate,
                                        Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects
    ) {
        Random rand = new Random();
        int attempts = 0;
        int maxAttempts = 50;

        while (pairsNeeded > 0 && attempts < maxAttempts && slots.size() >= 2) {
            int index1 = rand.nextInt(slots.size());
            int index2 = (index1 + 1) % slots.size();
            if (index2 == index1) index2 = (index2 + 1) % slots.size();

            Timetable slot1 = slots.get(index1);
            Timetable slot2 = slots.get(index2);

            if (slot1.getDayOfWeek() != slot2.getDayOfWeek() || Math.abs(slot1.getSlot() - slot2.getSlot()) != 1) {
                removeFromSlotMaps(slot1);
                removeFromSlotMaps(slot2);
                int[] newPair = findBestConsecutivePair(clazz, subject, teachers, teacherSubjects, rooms, classToTeacherSubjects);
                if (newPair != null) {
                    slot1.setDayOfWeek(newPair[0]);
                    slot1.setSlot(newPair[1]);
                    slot1.setDate(weekStartDate.plusDays(newPair[0] - 1));
                    slot2.setDayOfWeek(newPair[0]);
                    slot2.setSlot(newPair[1] + 1);
                    slot2.setDate(weekStartDate.plusDays(newPair[0] - 1));
                    updateSlotMaps(slot1);
                    updateSlotMaps(slot2);
                    pairsNeeded--;
                } else {
                    List<Timetable> otherSlots = timetable.stream()
                            .filter(t -> !t.equals(slot1) && !t.equals(slot2))
                            .filter(t -> {
                                SubjectResponse s = classToTeacherSubjects.values().stream()
                                        .flatMap(List::stream)
                                        .map(TeacherSubjectClassResponse::getSubject)
                                        .filter(sub -> sub.getId().equals(t.getSubjectId()))
                                        .findFirst()
                                        .orElse(null);
                                return s != null && !s.getName().equals("Class Activities") && !s.getName().equals("Flag Salute Session");
                            })
                            .collect(Collectors.toList());
                    if (!otherSlots.isEmpty()) {
                        Timetable swapSlot = otherSlots.get(rand.nextInt(otherSlots.size()));
                        removeFromSlotMaps(swapSlot);
                        int[] swapPair = findBestConsecutivePair(clazz, subject, teachers, teacherSubjects, rooms, classToTeacherSubjects);
                        if (swapPair != null) {
                            slot1.setDayOfWeek(swapPair[0]);
                            slot1.setSlot(swapPair[1]);
                            slot1.setDate(weekStartDate.plusDays(swapPair[0] - 1));
                            slot2.setDayOfWeek(swapPair[0]);
                            slot2.setSlot(swapPair[1] + 1);
                            slot2.setDate(weekStartDate.plusDays(swapPair[0] - 1));
                            updateSlotMaps(slot1);
                            updateSlotMaps(slot2);
                            swapSlot.setDayOfWeek(slot1.getDayOfWeek());
                            swapSlot.setSlot(slot1.getSlot());
                            swapSlot.setDate(weekStartDate.plusDays(slot1.getDayOfWeek() - 1));
                            updateSlotMaps(swapSlot);
                            pairsNeeded--;
                        } else {
                            updateSlotMaps(slot1);
                            updateSlotMaps(slot2);
                            updateSlotMaps(swapSlot);
                        }
                    } else {
                        updateSlotMaps(slot1);
                        updateSlotMaps(slot2);
                        pairsNeeded--;
                    }
                }
            } else {
                pairsNeeded--;
            }
            slots = timetable.stream()
                    .filter(t -> t.getClassId().equals(clazz.getClassId()) &&
                            t.getSubjectId().equals(subject.getId()))
                    .collect(Collectors.toList());
            attempts++;
        }
    }

    private int[] findBestSlotWithSwap(Timetable slot, TeacherResponse teacher, RoomResponse room, List<ClassResponse> classes,
                                       List<TeacherResponse> teachers, List<RoomResponse> rooms,
                                       Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects) {
        Random rand = new Random();
        ClassResponse clazz = classes.stream()
                .filter(c -> c.getClassId().equals(slot.getClassId()))
                .findFirst()
                .orElse(null);
        if (clazz == null) return null;
        SubjectResponse subject = classToTeacherSubjects.values().stream()
                .flatMap(List::stream)
                .map(TeacherSubjectClassResponse::getSubject)
                .filter(s -> s.getId().equals(slot.getSubjectId()))
                .findFirst()
                .orElse(null);
        if (subject == null) return null;
        boolean isMorningClass = clazz.getMainSession().equals("MORNING");
        boolean isMainSubject = subject.isMainSubject();
        List<Integer> validSlots = isMorningClass
                ? (isMainSubject ? Arrays.asList(1, 2, 3, 4, 5) : Arrays.asList(7, 8, 9, 10))
                : (isMainSubject ? Arrays.asList(6, 7, 8, 9, 10) : Arrays.asList(1, 2, 3, 4));

        List<Integer> days = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6));
        Collections.shuffle(days, rand);
        for (int day : days) {
            for (int s : validSlots) {
                if (isSlotValid(clazz, subject, day, s, teacher != null ? teacher : teachers.stream()
                        .filter(t -> t.getUserId().equals(slot.getTeacherId()))
                        .findFirst()
                        .orElse(null), room != null ? room : rooms.stream()
                        .filter(r -> r.getRoomId().equals(slot.getRoomId()))
                        .findFirst()
                        .orElse(null))) {
                    return new int[]{day, s};
                }
            }
        }

        List<Timetable> otherSlots = timetable.stream()
                .filter(t -> {
                    SubjectResponse s = classToTeacherSubjects.values().stream()
                            .flatMap(List::stream)
                            .map(TeacherSubjectClassResponse::getSubject)
                            .filter(sub -> sub.getId().equals(t.getSubjectId()))
                            .findFirst()
                            .orElse(null);
                    return s != null && !s.getName().equals("Class Activities") && !s.getName().equals("Flag Salute Session");
                })
                .collect(Collectors.toList());
        Collections.shuffle(otherSlots, rand);
        for (Timetable other : otherSlots) {
            ClassResponse otherClazz = classes.stream()
                    .filter(c -> c.getClassId().equals(other.getClassId()))
                    .findFirst()
                    .orElse(null);
            if (otherClazz == null || !otherClazz.getClassId().equals(clazz.getClassId())) continue;
            SubjectResponse otherSubject = classToTeacherSubjects.values().stream()
                    .flatMap(List::stream)
                    .map(TeacherSubjectClassResponse::getSubject)
                    .filter(s -> s.getId().equals(other.getSubjectId()))
                    .findFirst()
                    .orElse(null);
            if (otherSubject == null) continue;
            TeacherResponse otherTeacher = teachers.stream()
                    .filter(t -> t.getUserId().equals(other.getTeacherId()))
                    .findFirst()
                    .orElse(null);
            if (otherTeacher == null) continue;
            RoomResponse otherRoom = rooms.stream()
                    .filter(r -> r.getRoomId().equals(other.getRoomId()))
                    .findFirst()
                    .orElse(null);
            if (otherRoom == null) continue;

            int oldDay = slot.getDayOfWeek(), oldSlot = slot.getSlot();
            int newDay = other.getDayOfWeek(), newSlot = other.getSlot();

            removeFromSlotMaps(slot);
            removeFromSlotMaps(other);
            slot.setDayOfWeek(newDay);
            slot.setSlot(newSlot);
            other.setDayOfWeek(oldDay);
            other.setSlot(oldSlot);

            if (isSlotValid(clazz, subject, newDay, newSlot, teacher, room) &&
                    isSlotValid(otherClazz, otherSubject, oldDay, oldSlot, otherTeacher, otherRoom)) {
                updateSlotMaps(slot);
                updateSlotMaps(other);
                return new int[]{newDay, newSlot};
            } else {
                slot.setDayOfWeek(oldDay);
                slot.setSlot(oldSlot);
                other.setDayOfWeek(newDay);
                other.setSlot(newSlot);
                updateSlotMaps(slot);
                updateSlotMaps(other);
            }
        }
        return null;
    }

    private int[] findBestConsecutivePair(ClassResponse clazz, SubjectResponse subject, List<TeacherResponse> teachers,
                                          List<TeacherSubjectClassResponse> teacherSubjects, List<RoomResponse> rooms,
                                          Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects) {
        Random rand = new Random();
        TeacherResponse teacher = getTeacherForSubject(subject, clazz, teachers, teacherSubjects);
        RoomResponse room = clazz.getRoomResponse();
        boolean isMorningClass = clazz.getMainSession().equals("MORNING");
        boolean isMainSubject = subject.isMainSubject();
        List<Integer> validSlots = isMorningClass
                ? (isMainSubject ? Arrays.asList(1, 2, 3, 4) : Arrays.asList(7, 9))
                : (isMainSubject ? Arrays.asList(6, 7, 8, 9) : Arrays.asList(1, 3));

        List<Integer> days = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6));
        Collections.shuffle(days, rand);
        for (int day : days) {
            for (int slot : validSlots) {
                int nextSlot = slot + 1;
                String classKey1 = clazz.getClassId() + "_" + day + "_" + slot;
                String classKey2 = clazz.getClassId() + "_" + day + "_" + nextSlot;
                String teacherKey1 = teacher.getUserId() + "_" + day + "_" + slot;
                String teacherKey2 = teacher.getUserId() + "_" + day + "_" + nextSlot;
                String roomKey1 = room.getRoomId() + "_" + day + "_" + slot;
                String roomKey2 = room.getRoomId() + "_" + day + "_" + nextSlot;

                if (isSlotValid(clazz, subject, day, slot, teacher, room) &&
                        isSlotValid(clazz, subject, day, nextSlot, teacher, room) &&
                        classSlots.getOrDefault(classKey1, new HashSet<>()).isEmpty() &&
                        classSlots.getOrDefault(classKey2, new HashSet<>()).isEmpty() &&
                        teacherSlots.getOrDefault(teacherKey1, new HashSet<>()).isEmpty() &&
                        teacherSlots.getOrDefault(teacherKey2, new HashSet<>()).isEmpty() &&
                        roomSlots.getOrDefault(roomKey1, new HashSet<>()).isEmpty() &&
                        roomSlots.getOrDefault(roomKey2, new HashSet<>()).isEmpty()) {
                    return new int[]{day, slot};
                }
            }
        }
        return null;
    }

    public void mutate(List<ClassResponse> classes, List<TeacherResponse> teachers,
                       Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects,
                       List<RoomResponse> rooms, Long semesterId, Long schoolYearId, int weekNumber, LocalDate weekStartDate) {
        Random rand = new Random();
        if (timetable.size() <= 1) {
            log.warn("Timetable too small to mutate, size={}", timetable.size());
            return;
        }

        int mutationCount = rand.nextInt(3) + 1;
        log.debug("Applying {} mutations", mutationCount);

        for (int m = 0; m < mutationCount; m++) {
            int mutationType = rand.nextInt(5);
            switch (mutationType) {
                case 0: // Swap slots
                    int index1 = rand.nextInt(timetable.size());
                    int finalIndex = index1;
                    SubjectResponse subject1 = classToTeacherSubjects.values().stream()
                            .flatMap(List::stream)
                            .map(TeacherSubjectClassResponse::getSubject)
                            .filter(s -> s.getId().equals(timetable.get(finalIndex).getSubjectId()))
                            .findFirst()
                            .orElse(null);
                    while (subject1 == null || subject1.getName().equals("Class Activities") || subject1.getName().equals("Flag Salute Session")) {
                        index1 = rand.nextInt(timetable.size());
                        int finalIndex1 = index1;
                        subject1 = classToTeacherSubjects.values().stream()
                                .flatMap(List::stream)
                                .map(TeacherSubjectClassResponse::getSubject)
                                .filter(s -> s.getId().equals(timetable.get(finalIndex1).getSubjectId()))
                                .findFirst()
                                .orElse(null);
                    }
                    int index2 = rand.nextInt(timetable.size());
                    int finalIndex1 = index2;
                    SubjectResponse subject2 = classToTeacherSubjects.values().stream()
                            .flatMap(List::stream)
                            .map(TeacherSubjectClassResponse::getSubject)
                            .filter(s -> s.getId().equals(timetable.get(finalIndex1).getSubjectId()))
                            .findFirst()
                            .orElse(null);
                    while (index2 == index1 || subject2 == null || subject2.getName().equals("Class Activities") || subject2.getName().equals("Flag Salute Session")) {
                        index2 = rand.nextInt(timetable.size());
                        int finalIndex2 = index2;
                        subject2 = classToTeacherSubjects.values().stream()
                                .flatMap(List::stream)
                                .map(TeacherSubjectClassResponse::getSubject)
                                .filter(s -> s.getId().equals(timetable.get(finalIndex2).getSubjectId()))
                                .findFirst()
                                .orElse(null);
                    }

                    Timetable slot1 = timetable.get(index1);
                    Timetable slot2 = timetable.get(index2);
                    ClassResponse clazz1 = classes.stream()
                            .filter(c -> c.getClassId().equals(slot1.getClassId()))
                            .findFirst()
                            .orElse(null);
                    ClassResponse clazz2 = classes.stream()
                            .filter(c -> c.getClassId().equals(slot2.getClassId()))
                            .findFirst()
                            .orElse(null);
                    if (clazz1 == null || clazz2 == null) continue;
                    TeacherResponse teacher1 = teachers.stream()
                            .filter(t -> t.getUserId().equals(slot1.getTeacherId()))
                            .findFirst()
                            .orElse(null);
                    TeacherResponse teacher2 = teachers.stream()
                            .filter(t -> t.getUserId().equals(slot2.getTeacherId()))
                            .findFirst()
                            .orElse(null);
                    if (teacher1 == null || teacher2 == null) continue;
                    RoomResponse room1 = rooms.stream()
                            .filter(r -> r.getRoomId().equals(slot1.getRoomId()))
                            .findFirst()
                            .orElse(null);
                    RoomResponse room2 = rooms.stream()
                            .filter(r -> r.getRoomId().equals(slot2.getRoomId()))
                            .findFirst()
                            .orElse(null);
                    if (room1 == null || room2 == null) continue;

                    boolean isSwapValid = isSlotValid(clazz1, subject1, slot2.getDayOfWeek(), slot2.getSlot(), teacher1, room1) &&
                            isSlotValid(clazz2, subject2, slot1.getDayOfWeek(), slot1.getSlot(), teacher2, room2);

                    if (isSwapValid) {
                        removeFromSlotMaps(slot1);
                        removeFromSlotMaps(slot2);

                        int tempDay = slot1.getDayOfWeek();
                        int tempSlot = slot1.getSlot();
                        slot1.setDayOfWeek(slot2.getDayOfWeek());
                        slot1.setSlot(slot2.getSlot());
                        slot1.setDate(weekStartDate.plusDays(slot2.getDayOfWeek() - 1));
                        slot2.setDayOfWeek(tempDay);
                        slot2.setSlot(tempSlot);
                        slot2.setDate(weekStartDate.plusDays(tempDay - 1));

                        updateSlotMaps(slot1);
                        updateSlotMaps(slot2);
                    } else {
                        log.debug("Swap mutation skipped due to invalid slots");
                    }
                    break;

                case 1: // Reassign slot
                    index1 = rand.nextInt(timetable.size());
                    int finalIndex3 = index1;
                    subject1 = classToTeacherSubjects.values().stream()
                            .flatMap(List::stream)
                            .map(TeacherSubjectClassResponse::getSubject)
                            .filter(s -> s.getId().equals(timetable.get(finalIndex3).getSubjectId()))
                            .findFirst()
                            .orElse(null);
                    while (subject1 == null || subject1.getName().equals("Class Activities") || subject1.getName().equals("Flag Salute Session")) {
                        index1 = rand.nextInt(timetable.size());
                        int finalIndex4 = index1;
                        subject1 = classToTeacherSubjects.values().stream()
                                .flatMap(List::stream)
                                .map(TeacherSubjectClassResponse::getSubject)
                                .filter(s -> s.getId().equals(timetable.get(finalIndex4).getSubjectId()))
                                .findFirst()
                                .orElse(null);
                    }
                    slot1 = timetable.get(index1);
                    clazz1 = classes.stream()
                            .filter(c -> c.getClassId().equals(slot1.getClassId()))
                            .findFirst()
                            .orElse(null);
                    if (clazz1 == null) continue;
                    teacher1 = teachers.stream()
                            .filter(t -> t.getUserId().equals(slot1.getTeacherId()))
                            .findFirst()
                            .orElse(null);
                    if (teacher1 == null) continue;
                    room1 = rooms.stream()
                            .filter(r -> r.getRoomId().equals(slot1.getRoomId()))
                            .findFirst()
                            .orElse(null);
                    if (room1 == null) continue;

                    removeFromSlotMaps(slot1);

                    int[] newSlot = findBestSlotWithSwap(slot1, teacher1, room1, classes, teachers, rooms, classToTeacherSubjects);
                    if (newSlot != null) {
                        slot1.setDayOfWeek(newSlot[0]);
                        slot1.setSlot(newSlot[1]);
                        slot1.setDate(weekStartDate.plusDays(newSlot[0] - 1));
                        updateSlotMaps(slot1);
                    } else {
                        updateSlotMaps(slot1);
                    }
                    break;

                case 2: // Swap subjects
                    ClassResponse clazz = classes.get(rand.nextInt(classes.size()));
                    List<Timetable> classSlots = timetable.stream()
                            .filter(t -> t.getClassId().equals(clazz.getClassId()))
                            .filter(t -> {
                                SubjectResponse s = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList())
                                        .stream()
                                        .map(TeacherSubjectClassResponse::getSubject)
                                        .filter(sub -> sub.getId().equals(t.getSubjectId()))
                                        .findFirst()
                                        .orElse(null);
                                return s != null && !s.getName().equals("Class Activities") && !s.getName().equals("Flag Salute Session");
                            })
                            .collect(Collectors.toList());
                    if (classSlots.size() < 2) {
                        log.debug("Not enough slots to swap subjects in class {}", clazz.getClassName());
                        continue;
                    }

                    index1 = rand.nextInt(classSlots.size());
                    index2 = rand.nextInt(classSlots.size());
                    while (index2 == index1) {
                        index2 = rand.nextInt(classSlots.size());
                    }

                    slot1 = classSlots.get(index1);
                    slot2 = classSlots.get(index2);
                    subject1 = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList())
                            .stream()
                            .map(TeacherSubjectClassResponse::getSubject)
                            .filter(s -> s.getId().equals(slot1.getSubjectId()))
                            .findFirst()
                            .orElse(null);
                    subject2 = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList())
                            .stream()
                            .map(TeacherSubjectClassResponse::getSubject)
                            .filter(s -> s.getId().equals(slot2.getSubjectId()))
                            .findFirst()
                            .orElse(null);
                    if (subject1 == null || subject2 == null) continue;
                    teacher1 = teachers.stream()
                            .filter(t -> t.getUserId().equals(slot1.getTeacherId()))
                            .findFirst()
                            .orElse(null);
                    teacher2 = teachers.stream()
                            .filter(t -> t.getUserId().equals(slot2.getTeacherId()))
                            .findFirst()
                            .orElse(null);
                    if (teacher1 == null || teacher2 == null) continue;
                    room1 = rooms.stream()
                            .filter(r -> r.getRoomId().equals(slot1.getRoomId()))
                            .findFirst()
                            .orElse(null);
                    room2 = rooms.stream()
                            .filter(r -> r.getRoomId().equals(slot2.getRoomId()))
                            .findFirst()
                            .orElse(null);
                    if (room1 == null || room2 == null) continue;

                    Long tempSubjectId = slot1.getSubjectId();
                    slot1.setSubjectId(slot2.getSubjectId());
                    slot2.setSubjectId(tempSubjectId);

                    boolean isSubjectSwapValid = isSlotValid(clazz, subject2, slot1.getDayOfWeek(), slot1.getSlot(), teacher1, room1) &&
                            isSlotValid(clazz, subject1, slot2.getDayOfWeek(), slot2.getSlot(), teacher2, room2);

                    if (isSubjectSwapValid) {
                        updateSlotMaps(slot1);
                        updateSlotMaps(slot2);
                    } else {
                        slot1.setSubjectId(tempSubjectId);
                        slot2.setSubjectId(slot1.getSubjectId());
                        updateSlotMaps(slot1);
                        updateSlotMaps(slot2);
                    }
                    break;

                case 3: // Swap slots between paired days
                    clazz = classes.get(rand.nextInt(classes.size()));
                    int[][] dayPairs = {{1, 2}, {3, 4}, {5, 6}};
                    int[] pair = dayPairs[rand.nextInt(dayPairs.length)];
                    int day1 = pair[0];
                    int day2 = pair[1];
                    List<Timetable> slotsDay1 = timetable.stream()
                            .filter(t -> t.getClassId().equals(clazz.getClassId()) &&
                                    t.getDayOfWeek() == day1)
                            .filter(t -> {
                                SubjectResponse s = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList())
                                        .stream()
                                        .map(TeacherSubjectClassResponse::getSubject)
                                        .filter(sub -> sub.getId().equals(t.getSubjectId()))
                                        .findFirst()
                                        .orElse(null);
                                return s != null && !s.getName().equals("Class Activities") && !s.getName().equals("Flag Salute Session");
                            })
                            .collect(Collectors.toList());
                    List<Timetable> slotsDay2 = timetable.stream()
                            .filter(t -> t.getClassId().equals(clazz.getClassId()) &&
                                    t.getDayOfWeek() == day2)
                            .filter(t -> {
                                SubjectResponse s = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList())
                                        .stream()
                                        .map(TeacherSubjectClassResponse::getSubject)
                                        .filter(sub -> sub.getId().equals(t.getSubjectId()))
                                        .findFirst()
                                        .orElse(null);
                                return s != null && !s.getName().equals("Class Activities") && !s.getName().equals("Flag Salute Session");
                            })
                            .collect(Collectors.toList());
                    if (slotsDay1.isEmpty() || slotsDay2.isEmpty()) continue;

                    slot1 = slotsDay1.get(rand.nextInt(slotsDay1.size()));
                    slot2 = slotsDay2.get(rand.nextInt(slotsDay2.size()));
                    subject1 = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList())
                            .stream()
                            .map(TeacherSubjectClassResponse::getSubject)
                            .filter(s -> s.getId().equals(slot1.getSubjectId()))
                            .findFirst()
                            .orElse(null);
                    subject2 = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList())
                            .stream()
                            .map(TeacherSubjectClassResponse::getSubject)
                            .filter(s -> s.getId().equals(slot2.getSubjectId()))
                            .findFirst()
                            .orElse(null);
                    if (subject1 == null || subject2 == null) continue;
                    teacher1 = teachers.stream()
                            .filter(t -> t.getUserId().equals(slot1.getTeacherId()))
                            .findFirst()
                            .orElse(null);
                    teacher2 = teachers.stream()
                            .filter(t -> t.getUserId().equals(slot2.getTeacherId()))
                            .findFirst()
                            .orElse(null);
                    if (teacher1 == null || teacher2 == null) continue;
                    room1 = rooms.stream()
                            .filter(r -> r.getRoomId().equals(slot1.getRoomId()))
                            .findFirst()
                            .orElse(null);
                    room2 = rooms.stream()
                            .filter(r -> r.getRoomId().equals(slot2.getRoomId()))
                            .findFirst()
                            .orElse(null);
                    if (room1 == null || room2 == null) continue;

                    removeFromSlotMaps(slot1);
                    removeFromSlotMaps(slot2);

                    int tempDay = slot1.getDayOfWeek();
                    int tempSlot = slot1.getSlot();
                    slot1.setDayOfWeek(slot2.getDayOfWeek());
                    slot1.setSlot(slot2.getSlot());
                    slot1.setDate(weekStartDate.plusDays(slot2.getDayOfWeek() - 1));
                    slot2.setDayOfWeek(tempDay);
                    slot2.setSlot(tempSlot);
                    slot2.setDate(weekStartDate.plusDays(tempDay - 1));

                    if (isSlotValid(clazz, subject1, slot1.getDayOfWeek(), slot1.getSlot(), teacher1, room1) &&
                            isSlotValid(clazz, subject2, slot2.getDayOfWeek(), slot2.getSlot(), teacher2, room2)) {
                        updateSlotMaps(slot1);
                        updateSlotMaps(slot2);
                    } else {
                        slot1.setDayOfWeek(tempDay);
                        slot1.setSlot(tempSlot);
                        slot1.setDate(weekStartDate.plusDays(tempDay - 1));
                        slot2.setDayOfWeek(slot2.getDayOfWeek());
                        slot2.setSlot(slot2.getSlot());
                        slot2.setDate(weekStartDate.plusDays(slot2.getDayOfWeek() - 1));
                        updateSlotMaps(slot1);
                        updateSlotMaps(slot2);
                    }
                    break;

                case 4: // Reduce gaps
                    clazz = classes.get(rand.nextInt(classes.size()));
                    classSlots = timetable.stream()
                            .filter(t -> t.getClassId().equals(clazz.getClassId()))
                            .filter(t -> {
                                SubjectResponse s = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList())
                                        .stream()
                                        .map(TeacherSubjectClassResponse::getSubject)
                                        .filter(sub -> sub.getId().equals(t.getSubjectId()))
                                        .findFirst()
                                        .orElse(null);
                                return s != null && !s.getName().equals("Class Activities") && !s.getName().equals("Flag Salute Session");
                            })
                            .collect(Collectors.toList());
                    if (classSlots.isEmpty()) continue;

                    Timetable slot = classSlots.get(rand.nextInt(classSlots.size()));
                    subject1 = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList())
                            .stream()
                            .map(TeacherSubjectClassResponse::getSubject)
                            .filter(s -> s.getId().equals(slot.getSubjectId()))
                            .findFirst()
                            .orElse(null);
                    if (subject1 == null) continue;
                    int day = slot.getDayOfWeek();
                    List<Integer> assignedSlots = timetable.stream()
                            .filter(t -> t.getClassId().equals(clazz.getClassId()) &&
                                    t.getDayOfWeek() == day)
                            .map(Timetable::getSlot)
                            .collect(Collectors.toList());
                    boolean isMorningClass = clazz.getMainSession().equals("MORNING");
                    List<Integer> validSlots = subject1.isMainSubject()
                            ? (isMorningClass ? Arrays.asList(1, 2, 3, 4, 5) : Arrays.asList(6, 7, 8, 9, 10))
                            : (isMorningClass ? Arrays.asList(7, 8, 9, 10) : Arrays.asList(1, 2, 3, 4));

                    List<Integer> adjacentSlots = new ArrayList<>();
                    for (int s : validSlots) {
                        if (!assignedSlots.contains(s) && (assignedSlots.contains(s - 1) || assignedSlots.contains(s + 1))) {
                            adjacentSlots.add(s);
                        }
                    }
                    if (adjacentSlots.isEmpty()) continue;

                    int newSlot1 = adjacentSlots.get(rand.nextInt(adjacentSlots.size()));
                    teacher1 = teachers.stream()
                            .filter(t -> t.getUserId().equals(slot.getTeacherId()))
                            .findFirst()
                            .orElse(null);
                    if (teacher1 == null) continue;
                    room1 = rooms.stream()
                            .filter(r -> r.getRoomId().equals(slot.getRoomId()))
                            .findFirst()
                            .orElse(null);
                    if (room1 == null) continue;

                    if (isSlotValid(clazz, subject1, day, newSlot1, teacher1, room1)) {
                        removeFromSlotMaps(slot);
                        slot.setSlot(newSlot1);
                        slot.setDate(weekStartDate.plusDays(day - 1));
                        updateSlotMaps(slot);
                    }
                    break;
            }
        }

        repairConflicts(classes, teachers, classToTeacherSubjects, rooms, semesterId, schoolYearId, weekNumber, weekStartDate);
        calculateFitness(classes, teachers, classToTeacherSubjects, rooms);
        log.debug("Post-mutation fitness: {}", fitness);
    }


    public void localSearch(List<ClassResponse> classes, List<TeacherResponse> teachers,
                            Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects,
                            List<RoomResponse> rooms, Long semesterId, Long schoolYearId,
                            int weekNumber, LocalDate weekStartDate) {
        double bestFitness = fitness;
        List<Timetable> bestSlots = new ArrayList<>();
        List<int[]> bestChanges = new ArrayList<>();
        Random rand = new Random();

        int maxIterations = Math.min(timetable.size() * 10, 1000);
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            int searchType = rand.nextInt(3);
            if (searchType == 0) {
                int i = rand.nextInt(timetable.size());
                Timetable slot1 = timetable.get(i);
                SubjectResponse subject1 = classToTeacherSubjects.values().stream()
                        .flatMap(List::stream)
                        .map(TeacherSubjectClassResponse::getSubject)
                        .filter(s -> s.getId().equals(slot1.getSubjectId()))
                        .findFirst()
                        .orElse(null);
                if (subject1 == null || subject1.getName().equals("Class Activities") ||
                        subject1.getName().equals("Flag Salute Session")) continue;

                int j = rand.nextInt(timetable.size());
                Timetable slot2 = timetable.get(j);
                SubjectResponse subject2 = classToTeacherSubjects.values().stream()
                        .flatMap(List::stream)
                        .map(TeacherSubjectClassResponse::getSubject)
                        .filter(s -> s.getId().equals(slot2.getSubjectId()))
                        .findFirst()
                        .orElse(null);
                if (j == i || subject2 == null || subject2.getName().equals("Class Activities") ||
                        subject2.getName().equals("Flag Salute Session") ||
                        slot1.getClassId().equals(slot2.getClassId())) continue;

                removeFromSlotMaps(slot1);
                removeFromSlotMaps(slot2);

                int oldDay1 = slot1.getDayOfWeek(), oldSlot1 = slot1.getSlot();
                int oldDay2 = slot2.getDayOfWeek(), oldSlot2 = slot2.getSlot();

                slot1.setDayOfWeek(oldDay2);
                slot1.setSlot(oldSlot2);
                slot1.setDate(weekStartDate.plusDays(oldDay2 - 1));
                slot2.setDayOfWeek(oldDay1);
                slot2.setSlot(oldSlot1);
                slot2.setDate(weekStartDate.plusDays(oldDay1 - 1));

                updateSlotMaps(slot1);
                updateSlotMaps(slot2);
                calculateFitness(classes, teachers, classToTeacherSubjects, rooms);

                if (fitness < bestFitness) {
                    bestFitness = fitness;
                    bestSlots.clear();
                    bestChanges.clear();
                    bestSlots.add(new Timetable(slot1));
                    bestSlots.add(new Timetable(slot2));
                    bestChanges.add(new int[]{oldDay1, oldSlot1, oldDay2, oldSlot2});
                }

                removeFromSlotMaps(slot1);
                removeFromSlotMaps(slot2);
                slot1.setDayOfWeek(oldDay1);
                slot1.setSlot(oldSlot1);
                slot1.setDate(weekStartDate.plusDays(oldDay1 - 1));
                slot2.setDayOfWeek(oldDay2);
                slot2.setSlot(oldSlot2);
                slot2.setDate(weekStartDate.plusDays(oldDay2 - 1));
                updateSlotMaps(slot1);
                updateSlotMaps(slot2);
            } else if (searchType == 1) {
                ClassResponse clazz = classes.get(rand.nextInt(classes.size()));
                List<Timetable> classSlots = timetable.stream()
                        .filter(t -> t.getClassId().equals(clazz.getClassId()))
                        .filter(t -> {
                            SubjectResponse s = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList())
                                    .stream()
                                    .map(TeacherSubjectClassResponse::getSubject)
                                    .filter(sub -> sub.getId().equals(t.getSubjectId()))
                                    .findFirst()
                                    .orElse(null);
                            return s != null && !s.getName().equals("Class Activities") &&
                                    !s.getName().equals("Flag Salute Session");
                        })
                        .collect(Collectors.toList());
                if (classSlots.size() < 2) continue;

                int i = rand.nextInt(classSlots.size());
                int j = rand.nextInt(classSlots.size());
                if (i == j) continue;

                Timetable slot1 = classSlots.get(i);
                Timetable slot2 = classSlots.get(j);

                removeFromSlotMaps(slot1);
                removeFromSlotMaps(slot2);

                int oldDay1 = slot1.getDayOfWeek(), oldSlot1 = slot1.getSlot();
                int oldDay2 = slot2.getDayOfWeek(), oldSlot2 = slot2.getSlot();

                slot1.setDayOfWeek(oldDay2);
                slot1.setSlot(oldSlot2);
                slot1.setDate(weekStartDate.plusDays(oldDay2 - 1));
                slot2.setDayOfWeek(oldDay1);
                slot2.setSlot(oldSlot1);
                slot2.setDate(weekStartDate.plusDays(oldDay1 - 1));

                updateSlotMaps(slot1);
                updateSlotMaps(slot2);
                calculateFitness(classes, teachers, classToTeacherSubjects, rooms);

                if (fitness < bestFitness) {
                    bestFitness = fitness;
                    bestSlots.clear();
                    bestChanges.clear();
                    bestSlots.add(new Timetable(slot1));
                    bestSlots.add(new Timetable(slot2));
                    bestChanges.add(new int[]{oldDay1, oldSlot1, oldDay2, oldSlot2});
                }

                removeFromSlotMaps(slot1);
                removeFromSlotMaps(slot2);
                slot1.setDayOfWeek(oldDay1);
                slot1.setSlot(oldSlot1);
                slot1.setDate(weekStartDate.plusDays(oldDay1 - 1));
                slot2.setDayOfWeek(oldDay2);
                slot2.setSlot(oldSlot2);
                slot2.setDate(weekStartDate.plusDays(oldDay2 - 1));
                updateSlotMaps(slot1);
                updateSlotMaps(slot2);
            } else {
                ClassResponse clazz = classes.get(rand.nextInt(classes.size()));
                int[][] dayPairs = {{1, 2}, {3, 4}, {5, 6}};
                int[] pair = dayPairs[rand.nextInt(dayPairs.length)];
                int day1 = pair[0];
                int day2 = pair[1];
                List<Timetable> slotsDay1 = timetable.stream()
                        .filter(t -> t.getClassId().equals(clazz.getClassId()) &&
                                t.getDayOfWeek() == day1)
                        .filter(t -> {
                            SubjectResponse s = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList())
                                    .stream()
                                    .map(TeacherSubjectClassResponse::getSubject)
                                    .filter(sub -> sub.getId().equals(t.getSubjectId()))
                                    .findFirst()
                                    .orElse(null);
                            return s != null && !s.getName().equals("Class Activities") &&
                                    !s.getName().equals("Flag Salute Session");
                        })
                        .collect(Collectors.toList());
                List<Timetable> slotsDay2 = timetable.stream()
                        .filter(t -> t.getClassId().equals(clazz.getClassId()) &&
                                t.getDayOfWeek() == day2)
                        .filter(t -> {
                            SubjectResponse s = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList())
                                    .stream()
                                    .map(TeacherSubjectClassResponse::getSubject)
                                    .filter(sub -> sub.getId().equals(t.getSubjectId()))
                                    .findFirst()
                                    .orElse(null);
                            return s != null && !s.getName().equals("Class Activities") &&
                                    !s.getName().equals("Flag Salute Session");
                        })
                        .collect(Collectors.toList());
                if (slotsDay1.isEmpty() || slotsDay2.isEmpty()) continue;

                Timetable slot1 = slotsDay1.get(rand.nextInt(slotsDay1.size()));
                Timetable slot2 = slotsDay2.get(rand.nextInt(slotsDay2.size()));

                removeFromSlotMaps(slot1);
                removeFromSlotMaps(slot2);

                int oldDay1 = slot1.getDayOfWeek(), oldSlot1 = slot1.getSlot();
                int oldDay2 = slot2.getDayOfWeek(), oldSlot2 = slot2.getSlot();

                slot1.setDayOfWeek(oldDay2);
                slot1.setSlot(oldSlot2);
                slot1.setDate(weekStartDate.plusDays(oldDay2 - 1));
                slot2.setDayOfWeek(oldDay1);
                slot2.setSlot(oldSlot1);
                slot2.setDate(weekStartDate.plusDays(oldDay1 - 1));

                updateSlotMaps(slot1);
                updateSlotMaps(slot2);
                calculateFitness(classes, teachers, classToTeacherSubjects, rooms);

                if (fitness < bestFitness) {
                    bestFitness = fitness;
                    bestSlots.clear();
                    bestChanges.clear();
                    bestSlots.add(new Timetable(slot1));
                    bestSlots.add(new Timetable(slot2));
                    bestChanges.add(new int[]{oldDay1, oldSlot1, oldDay2, oldSlot2});
                }

                removeFromSlotMaps(slot1);
                removeFromSlotMaps(slot2);
                slot1.setDayOfWeek(oldDay1);
                slot1.setSlot(oldSlot1);
                slot1.setDate(weekStartDate.plusDays(oldDay1 - 1));
                slot2.setDayOfWeek(oldDay2);
                slot2.setSlot(oldSlot2);
                slot2.setDate(weekStartDate.plusDays(oldDay2 - 1));
                updateSlotMaps(slot1);
                updateSlotMaps(slot2);
            }
        }

        if (!bestSlots.isEmpty()) {
            Timetable slot1 = bestSlots.get(0);
            Timetable slot2 = bestSlots.get(1);
            int[] changes = bestChanges.get(0);
            Timetable actualSlot1 = timetable.stream()
                    .filter(t -> t.equals(slot1))
                    .findFirst()
                    .orElse(null);
            Timetable actualSlot2 = timetable.stream()
                    .filter(t -> t.equals(slot2))
                    .findFirst()
                    .orElse(null);
            if (actualSlot1 != null && actualSlot2 != null) {
                removeFromSlotMaps(actualSlot1);
                removeFromSlotMaps(actualSlot2);
                actualSlot1.setDayOfWeek(changes[2]);
                actualSlot1.setSlot(changes[3]);
                actualSlot1.setDate(weekStartDate.plusDays(changes[2] - 1));
                actualSlot2.setDayOfWeek(changes[0]);
                actualSlot2.setSlot(changes[1]);
                actualSlot2.setDate(weekStartDate.plusDays(changes[0] - 1));
                updateSlotMaps(actualSlot1);
                updateSlotMaps(actualSlot2);
                repairConflicts(classes, teachers, classToTeacherSubjects, rooms,
                        semesterId, schoolYearId, weekNumber, weekStartDate);
                calculateFitness(classes, teachers, classToTeacherSubjects, rooms);
                log.debug("Local search improved fitness to {}", fitness);
            }
        }
    }

    private long countConsecutiveSlots(ClassResponse clazz, SubjectResponse subject) {
        long consecutiveCount = 0;
        for (int day = 1; day <= 6; day++) {
            for (int slot = 1; slot < 10; slot++) {
                String key1 = clazz.getClassId() + "_" + day + "_" + slot;
                String key2 = clazz.getClassId() + "_" + day + "_" + (slot + 1);
                int finalDay = day;
                int finalSlot = slot;
                if (!classSlots.getOrDefault(key1, new HashSet<>()).isEmpty() &&
                        !classSlots.getOrDefault(key2, new HashSet<>()).isEmpty() &&
                        timetable.stream().anyMatch(t -> t.getClassId().equals(clazz.getClassId()) &&
                                t.getSubjectId().equals(subject.getId()) &&
                                t.getDayOfWeek() == finalDay &&
                                t.getSlot() == finalSlot) &&
                        timetable.stream().anyMatch(t -> t.getClassId().equals(clazz.getClassId()) &&
                                t.getSubjectId().equals(subject.getId()) &&
                                t.getDayOfWeek() == finalDay &&
                                t.getSlot() == finalSlot + 1)) {
                    consecutiveCount++;
                }
            }
        }
        return consecutiveCount;
    }
}