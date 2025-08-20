package com.datn.timetable_service.service;

import com.datn.timetable_service.client.SchoolServiceClient;
import com.datn.timetable_service.client.SubjectServiceClient;
import com.datn.timetable_service.client.UserServiceClient;
import com.datn.timetable_service.dto.SchoolService.ClassResponse;
import com.datn.timetable_service.dto.SchoolService.RoomResponse;
import com.datn.timetable_service.dto.SchoolService.SchoolYearResponse;
import com.datn.timetable_service.dto.SchoolService.SemesterResponse;
import com.datn.timetable_service.dto.UserService.TeacherResponse;
import com.datn.timetable_service.dto.subject_service.RawTeacherSubjectClassResponse;
import com.datn.timetable_service.dto.subject_service.SubjectResponse;
import com.datn.timetable_service.dto.subject_service.TeacherSubjectClassResponse;
import com.datn.timetable_service.model.Timetable;
import com.datn.timetable_service.repository.TimetableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TimetableService {
    private static final int POPULATION_SIZE = 200;
    private static final int MAX_GENERATIONS = 1000;
    private static final double MUTATION_RATE = 0.3;
    private static final int TOURNAMENT_SIZE = 7;
    private static final int STAGNATION_THRESHOLD = 50;

    @Autowired
    private SchoolServiceClient schoolServiceClient;
    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private TimetableRepository timetableRepo;
    @Autowired
    private SubjectServiceClient subjectServiceClient;

    public List<Timetable> generateTimetable(Long semesterId, Long schoolYearId, Long weekNumber) {
        log.info("Starting timetable generation for semesterId: {}, schoolYearId: {}, weekNumber: {}",
                semesterId, schoolYearId, weekNumber);

        if (semesterId == null || schoolYearId == null || weekNumber == null) {
            throw new IllegalArgumentException("semesterId, schoolYearId, and weekNumber must not be null");
        }

        SemesterResponse semester = schoolServiceClient.getSemesterById(semesterId).getResult();
        SchoolYearResponse schoolYear = schoolServiceClient.getSchoolYearById(schoolYearId).getResult();

        if (semester == null || schoolYear == null) {
            throw new IllegalArgumentException("Invalid semesterId or schoolYearId");
        }

        LocalDate semesterStart = semester.getStartDate();
        LocalDate semesterEnd = semester.getEndDate();
        LocalDate weekStartDate = calculateWeekStartDate(semesterStart, weekNumber.intValue());

        if (weekStartDate.isBefore(semesterStart) || weekStartDate.isAfter(semesterEnd)) {
            throw new IllegalArgumentException("Week number " + weekNumber + " is outside the semester date range");
        }

        List<ClassResponse> classes = schoolServiceClient.getClassBySchoolYear(schoolYearId).getResult();
        List<TeacherResponse> teachers = userServiceClient.getAllTeachers().getResult();
        List<RoomResponse> rooms = schoolServiceClient.getAllRoomIsActive(0, 1000, "roomFloor", "asc").getResult().getContent();
        Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects = new HashMap<>();

        if (classes == null || teachers == null || rooms == null) {
            throw new IllegalStateException("Failed to fetch classes, teachers, or rooms");
        }

        for (ClassResponse clazz : classes) {
            List<RawTeacherSubjectClassResponse> rawList = subjectServiceClient.getTeacherSubjectByClass(clazz.getClassId()).getResult();
            List<TeacherSubjectClassResponse> teacherSubjects = rawList.stream()
                    .map(this::convert)
                    .collect(Collectors.toList());
            classToTeacherSubjects.put(clazz, teacherSubjects != null ? teacherSubjects : Collections.emptyList());

            int totalWeeklySlots = teacherSubjects.stream()
                    .map(TeacherSubjectClassResponse::getSubject)
                    .filter(s -> !s.getName().equals("Class Activities") && !s.getName().equals("Flag Salute Session") && s.isMainSubject())
                    .mapToInt(SubjectResponse::getWeeklySlots)
                    .sum();
            int maxSlotsPerWeek = 6 * 5; // 6 days * 5 slots/day
            if (totalWeeklySlots > maxSlotsPerWeek) {
                log.error("Class {} requires {} slots/week, exceeding maximum of {}. Adjusting weeklySlots.", clazz.getClassName(), totalWeeklySlots, maxSlotsPerWeek);
                throw new IllegalStateException("Total weekly slots for class " + clazz.getClassName() + " exceeds maximum allowed");
            }
        }

        List<Chromosome> population = initializePopulation(classes, classToTeacherSubjects, teachers, rooms,
                semesterId, schoolYearId, weekNumber.intValue(), weekStartDate);
        Collections.sort(population, Comparator.comparingDouble(Chromosome::getFitness));
        Chromosome bestSolution = new Chromosome(population.get(0));
        int stagnationCount = 0;

        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            log.debug("Starting generation {}", generation + 1);
            population = evolvePopulation(population, classes, classToTeacherSubjects, teachers, rooms,
                    semesterId, schoolYearId, weekNumber.intValue(), weekStartDate);
            Collections.sort(population, Comparator.comparingDouble(Chromosome::getFitness));
            Chromosome currentBest = population.get(0);

            if (currentBest.getFitness() < bestSolution.getFitness()) {
                bestSolution = new Chromosome(currentBest);
                stagnationCount = 0;
                log.debug("New best solution found with fitness: {}", bestSolution.getFitness());
            } else {
                stagnationCount++;
            }

            log.debug("Generation {}: Best fitness = {}", generation + 1, bestSolution.getFitness());

            if (stagnationCount >= STAGNATION_THRESHOLD) {
                log.info("Stagnation detected at generation {}. Restarting 50% of population.", generation + 1);
                population = restartPopulation(population, classes, classToTeacherSubjects, teachers, rooms,
                        semesterId, schoolYearId, weekNumber.intValue(), weekStartDate);
                stagnationCount = 0;
            }

            if (bestSolution.getFitness() == 0) {
                log.info("Found optimal solution at generation {}", generation + 1);
                break;
            }

            Chromosome bestSolutionCopy = new Chromosome(bestSolution);
            bestSolutionCopy.localSearch(classes, teachers, classToTeacherSubjects, rooms,
                    semesterId, schoolYearId, weekNumber.intValue(), weekStartDate);
            if (bestSolutionCopy.getFitness() < bestSolution.getFitness()) {
                bestSolution = bestSolutionCopy;
                log.debug("Local search improved bestSolution to fitness: {}", bestSolution.getFitness());
            }

            population.set(0, new Chromosome(bestSolution));
            Collections.sort(population, Comparator.comparingDouble(Chromosome::getFitness));
        }

        List<Timetable> result = bestSolution.getTimetable();
        timetableRepo.saveAll(result);
        log.info("Timetable generation completed with {} slots saved", result.size());
        return result;
    }

    public TeacherSubjectClassResponse convert(RawTeacherSubjectClassResponse raw) {
        TeacherResponse teacher = TeacherResponse.builder()
                .userId(raw.getTeacherId())
                .fullName(raw.getFullName())
                .email(raw.getEmail())
                .phoneNumber(raw.getPhoneNumber())
                .address(raw.getAddress())
                .build();

        SubjectResponse subject = new SubjectResponse();
        subject.setId(raw.getSubjectId());
        subject.setName(raw.getSubjectName());
        subject.setWeeklySlots(raw.getWeeklySlots());
        subject.setMaxSlotsPerSession(raw.getMaxSlotsPerSession());
        subject.setPreferConsecutive(raw.isPreferConsecutive());
        subject.setMainSubject(raw.isMainSubject());
        subject.setSpecialSlot(raw.getSpecialSlot());

        ClassResponse clazz = ClassResponse.builder()
                .classId(raw.getClassId())
                .className(raw.getClassName())
                .schoolYearId(raw.getSchoolYearId())
                .teacher(raw.getHomeRoomTeacher())
                .build();

        return TeacherSubjectClassResponse.builder()
                .teacher(teacher)
                .subject(subject)
                .clazz(clazz)
                .build();
    }

    private LocalDate calculateWeekStartDate(LocalDate semesterStart, int weekNumber) {
        return semesterStart.plusWeeks(weekNumber - 1)
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
    }

    private List<Chromosome> initializePopulation(List<ClassResponse> classes,
                                                  Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects,
                                                  List<TeacherResponse> teachers, List<RoomResponse> rooms,
                                                  Long semesterId, Long schoolYearId, int weekNumber, LocalDate weekStartDate) {
        List<Chromosome> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Chromosome chromosome = new Chromosome();
            for (ClassResponse clazz : classes) {
                List<TeacherSubjectClassResponse> teacherSubjects = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList());
                List<SubjectResponse> subjects = teacherSubjects.stream()
                        .map(TeacherSubjectClassResponse::getSubject)
                        .distinct()
                        .collect(Collectors.toList());
                chromosome.addFixedSlots(clazz, subjects, teachers, teacherSubjects,
                        semesterId, schoolYearId, weekNumber, weekStartDate);
                chromosome.fillRandomSlots(clazz, subjects, teachers, teacherSubjects,
                        semesterId, schoolYearId, weekNumber, weekStartDate);
            }
            chromosome.calculateFitness(classes, teachers, classToTeacherSubjects, rooms);
            chromosome.repairConflicts(classes, teachers, classToTeacherSubjects, rooms,
                    semesterId, schoolYearId, weekNumber, weekStartDate);
            population.add(chromosome);
        }
        log.debug("Initialized population with {} chromosomes", population.size());
        return population;
    }

    private List<Chromosome> evolvePopulation(List<Chromosome> population, List<ClassResponse> classes,
                                              Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects,
                                              List<TeacherResponse> teachers, List<RoomResponse> rooms,
                                              Long semesterId, Long schoolYearId, int weekNumber, LocalDate weekStartDate) {
        List<Chromosome> newPopulation = new ArrayList<>();
        Collections.sort(population, Comparator.comparingDouble(Chromosome::getFitness));

        int eliteSize = (int) (POPULATION_SIZE * 0.1);
        newPopulation.addAll(population.subList(0, eliteSize));
        double bestParentFitness = population.get(0).getFitness();
        double fitnessTolerance = bestParentFitness * 1.5;

        Random rand = new Random();
        while (newPopulation.size() < POPULATION_SIZE) {
            Chromosome parent1 = tournamentSelection(population);
            Chromosome parent2 = tournamentSelection(population);
            Chromosome child = uniformCrossover(parent1, parent2, classes, teachers, classToTeacherSubjects, rooms,
                    semesterId, schoolYearId, weekNumber, weekStartDate);

            if (Math.random() < MUTATION_RATE) {
                child.mutate(classes, teachers, classToTeacherSubjects, rooms,
                        semesterId, schoolYearId, weekNumber, weekStartDate);
            }
            child.repairConflicts(classes, teachers, classToTeacherSubjects, rooms,
                    semesterId, schoolYearId, weekNumber, weekStartDate);
            child.calculateFitness(classes, teachers, classToTeacherSubjects, rooms);

            if (child.getFitness() <= fitnessTolerance || child.getFitness() < bestParentFitness) {
                newPopulation.add(child);
            } else {
                newPopulation.add(parent1.getFitness() < parent2.getFitness() ? parent1 : parent2);
            }
        }

        return newPopulation.subList(0, POPULATION_SIZE);
    }

    private Chromosome tournamentSelection(List<Chromosome> population) {
        Random rand = new Random();
        Chromosome best = population.get(rand.nextInt(population.size()));
        for (int i = 1; i < TOURNAMENT_SIZE; i++) {
            Chromosome contender = population.get(rand.nextInt(population.size()));
            if (contender.getFitness() < best.getFitness()) {
                best = contender;
            }
        }
        return best;
    }

    private List<Chromosome> restartPopulation(List<Chromosome> population, List<ClassResponse> classes,
                                               Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects,
                                               List<TeacherResponse> teachers, List<RoomResponse> rooms,
                                               Long semesterId, Long schoolYearId, int weekNumber, LocalDate weekStartDate) {
        population.sort(Comparator.comparingDouble(Chromosome::getFitness));
        int keepSize = POPULATION_SIZE / 2;
        List<Chromosome> newPopulation = new ArrayList<>(population.subList(0, keepSize));

        for (int i = 0; i < POPULATION_SIZE - keepSize; i++) {
            Chromosome chromosome = new Chromosome();
            for (ClassResponse clazz : classes) {
                List<TeacherSubjectClassResponse> teacherSubjects = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList());
                List<SubjectResponse> subjects = teacherSubjects.stream()
                        .map(TeacherSubjectClassResponse::getSubject)
                        .distinct()
                        .collect(Collectors.toList());
                chromosome.addFixedSlots(clazz, subjects, teachers, teacherSubjects,
                        semesterId, schoolYearId, weekNumber, weekStartDate);
                chromosome.fillRandomSlots(clazz, subjects, teachers, teacherSubjects,
                        semesterId, schoolYearId, weekNumber, weekStartDate);
            }
            chromosome.repairConflicts(classes, teachers, classToTeacherSubjects, rooms,
                    semesterId, schoolYearId, weekNumber, weekStartDate);
            chromosome.calculateFitness(classes, teachers, classToTeacherSubjects, rooms);
            newPopulation.add(chromosome);
        }
        return newPopulation;
    }

    private Chromosome uniformCrossover(Chromosome parent1, Chromosome parent2, List<ClassResponse> classes,
                                        List<TeacherResponse> teachers, Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects,
                                        List<RoomResponse> rooms, Long semesterId, Long schoolYearId, int weekNumber, LocalDate weekStartDate) {
        Chromosome child = new Chromosome();
        Random rand = new Random();
        List<Timetable> timetable1 = parent1.getTimetable();
        List<Timetable> timetable2 = parent2.getTimetable();

        Map<String, Timetable> uniqueSlots = new HashMap<>();
        double parent1Weight = parent1.getFitness() < parent2.getFitness() ? 0.7 : 0.5;

        for (Timetable slot : timetable1) {
            String key = slot.getClassId() + "_" + slot.getDayOfWeek() + "_" + slot.getSlot();
            String teacherKey = slot.getTeacherId() + "_" + slot.getDayOfWeek() + "_" + slot.getSlot();
            String roomKey = slot.getRoomId() + "_" + slot.getDayOfWeek() + "_" + slot.getSlot();

            ClassResponse clazz = classes.stream()
                    .filter(c -> c.getClassId().equals(slot.getClassId()))
                    .findFirst()
                    .orElse(null);
            SubjectResponse subject = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList())
                    .stream()
                    .map(TeacherSubjectClassResponse::getSubject)
                    .filter(s -> s.getId().equals(slot.getSubjectId()))
                    .findFirst()
                    .orElse(null);
            TeacherResponse teacher = teachers.stream()
                    .filter(t -> t.getUserId().equals(slot.getTeacherId()))
                    .findFirst()
                    .orElse(null);
            RoomResponse room = rooms.stream()
                    .filter(r -> r.getRoomId().equals(slot.getRoomId()))
                    .findFirst()
                    .orElse(null);

            if (clazz == null || subject == null || teacher == null || room == null) continue;

            if (rand.nextDouble() < parent1Weight &&
                    uniqueSlots.get(key) == null &&
                    child.getTeacherSlots().getOrDefault(teacherKey, new HashSet<>()).isEmpty() &&
                    child.getRoomSlots().getOrDefault(roomKey, new HashSet<>()).isEmpty() &&
                    child.isSlotValid(clazz, subject, slot.getDayOfWeek(), slot.getSlot(), teacher, room)) {
                Timetable newSlot = new Timetable(slot);
                newSlot.setSemesterId(semesterId);
                newSlot.setSchoolYearId(schoolYearId);
                newSlot.setWeek(weekNumber);
                newSlot.setDate(weekStartDate.plusDays(slot.getDayOfWeek() - 1));
                child.getTimetable().add(newSlot);
                child.updateSlotMaps(newSlot);
                uniqueSlots.put(key, newSlot);
            }
        }

        for (Timetable slot : timetable2) {
            String key = slot.getClassId() + "_" + slot.getDayOfWeek() + "_" + slot.getSlot();
            String teacherKey = slot.getTeacherId() + "_" + slot.getDayOfWeek() + "_" + slot.getSlot();
            String roomKey = slot.getRoomId() + "_" + slot.getDayOfWeek() + "_" + slot.getSlot();

            ClassResponse clazz = classes.stream()
                    .filter(c -> c.getClassId().equals(slot.getClassId()))
                    .findFirst()
                    .orElse(null);
            SubjectResponse subject = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList())
                    .stream()
                    .map(TeacherSubjectClassResponse::getSubject)
                    .filter(s -> s.getId().equals(slot.getSubjectId()))
                    .findFirst()
                    .orElse(null);
            TeacherResponse teacher = teachers.stream()
                    .filter(t -> t.getUserId().equals(slot.getTeacherId()))
                    .findFirst()
                    .orElse(null);
            RoomResponse room = rooms.stream()
                    .filter(r -> r.getRoomId().equals(slot.getRoomId()))
                    .findFirst()
                    .orElse(null);

            if (clazz == null || subject == null || teacher == null || room == null) continue;

            if (uniqueSlots.get(key) == null &&
                    child.getTeacherSlots().getOrDefault(teacherKey, new HashSet<>()).isEmpty() &&
                    child.getRoomSlots().getOrDefault(roomKey, new HashSet<>()).isEmpty() &&
                    child.isSlotValid(clazz, subject, slot.getDayOfWeek(), slot.getSlot(), teacher, room)) {
                Timetable newSlot = new Timetable(slot);
                newSlot.setSemesterId(semesterId);
                newSlot.setSchoolYearId(schoolYearId);
                newSlot.setWeek(weekNumber);
                newSlot.setDate(weekStartDate.plusDays(slot.getDayOfWeek() - 1));
                child.getTimetable().add(newSlot);
                child.updateSlotMaps(newSlot);
                uniqueSlots.put(key, newSlot);
            }
        }

        child.repairConflicts(classes, teachers, classToTeacherSubjects, rooms,
                semesterId, schoolYearId, weekNumber, weekStartDate);
        return child;
    }
}