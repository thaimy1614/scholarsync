package com.datn.timetable_service.service;

import com.datn.timetable_service.client.SchoolServiceClient;
import com.datn.timetable_service.client.SubjectServiceClient;
import com.datn.timetable_service.client.UserServiceClient;
import com.datn.timetable_service.dto.SchoolService.ClassResponse;
import com.datn.timetable_service.dto.SchoolService.RoomResponse;
import com.datn.timetable_service.dto.SchoolService.SchoolYearResponse;
import com.datn.timetable_service.dto.SchoolService.SemesterResponse;
import com.datn.timetable_service.dto.UserService.TeacherResponse;
import com.datn.timetable_service.dto.request.ExamScheduleCreationRequest;
import com.datn.timetable_service.dto.request.GetExamScheduleResponse;
import com.datn.timetable_service.dto.subject_service.RawTeacherSubjectClassResponse;
import com.datn.timetable_service.dto.subject_service.SubjectResponse;
import com.datn.timetable_service.dto.subject_service.TeacherSubjectClassResponse;
import com.datn.timetable_service.model.ExamSchedule;
import com.datn.timetable_service.model.ExamSession;
import com.datn.timetable_service.model.SubjectExamInfo;
import com.datn.timetable_service.repository.ExamScheduleRepository;
import com.datn.timetable_service.repository.ExamSessionRepository;
import com.datn.timetable_service.repository.SubjectExamInfoRepository;
import lombok.extern.slf4j.Slf4j;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExamScheduleService {
    private static final int POPULATION_SIZE = 200;
    private static final int MAX_GENERATIONS = 1000;
    private static final double MUTATION_RATE = 0.3;
    private static final int TOURNAMENT_SIZE = 7;
    private static final int STAGNATION_THRESHOLD = 50;
    public static final List<LocalTime> EXAM_TIMES = Arrays.asList(
            LocalTime.of(7, 0), LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(10, 0),
            LocalTime.of(13, 0), LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0)
    );
    public static final LocalTime MORNING_END = LocalTime.of(11, 0);
    public static final LocalTime AFTERNOON_END = LocalTime.of(17, 0);

    @Autowired
    private SchoolServiceClient schoolServiceClient;
    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private SubjectServiceClient subjectServiceClient;
    @Autowired
    private ExamScheduleRepository examScheduleRepo;
    @Autowired
    private ExamSessionRepository examSessionRepo;
    @Autowired
    private SubjectExamInfoRepository subjectExamInfoRepo;

    public List<ExamSchedule> generateExamSchedule(Long examSessionId) {
        log.info("Starting exam schedule generation for examSessionId: {}", examSessionId);

        if (examSessionId == null) {
            throw new IllegalArgumentException("examSessionId must not be null");
        }

        ExamSession examSession = examSessionRepo.findById(examSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid examSessionId"));

        Long semesterId = examSession.getSemesterId();
        Long schoolYearId = examSession.getSchoolYearId();
        LocalDate startDate = examSession.getStartDate();
        LocalDate endDate = examSession.getEndDate();

        SemesterResponse semester = schoolServiceClient.getSemesterById(semesterId).getResult();
        SchoolYearResponse schoolYear = schoolServiceClient.getSchoolYearById(schoolYearId).getResult();

        if (semester == null || schoolYear == null) {
            throw new IllegalArgumentException("Invalid semesterId or schoolYearId");
        }

        LocalDate semesterStart = semester.getStartDate();
        LocalDate semesterEnd = semester.getEndDate();
//        if (startDate.isBefore(semesterStart) || endDate.isAfter(semesterEnd)) {
//            throw new IllegalArgumentException("Exam session dates are outside the semester date range");
//        }

        List<ClassResponse> classes = schoolServiceClient.getClassBySchoolYear(schoolYearId).getResult();
        List<TeacherResponse> teachers = userServiceClient.getAllTeachers().getResult();
        List<RoomResponse> rooms = schoolServiceClient.getAllRoomIsActive(0, 1000, "roomFloor", "asc").getResult().getContent();
        List<SubjectExamInfo> subjectExamInfos = subjectExamInfoRepo.findAll();
        Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects = new HashMap<>();

        if (classes == null || teachers == null || rooms == null || subjectExamInfos == null) {
            throw new IllegalStateException("Failed to fetch classes, teachers, rooms, or subject exam info");
        }

        for (ClassResponse clazz : classes) {
            List<RawTeacherSubjectClassResponse> rawList = subjectServiceClient.getTeacherSubjectByClass(clazz.getClassId()).getResult();
            List<TeacherSubjectClassResponse> teacherSubjects = rawList.stream()
                    .map(this::convert)
                    .collect(Collectors.toList());
            classToTeacherSubjects.put(clazz, teacherSubjects != null ? teacherSubjects : Collections.emptyList());
        }

        List<ExamChromosome> population = initializePopulation(classes, classToTeacherSubjects, teachers, rooms,
                semesterId, schoolYearId, startDate, endDate, subjectExamInfos);
        Collections.sort(population, Comparator.comparingDouble(ExamChromosome::getFitness));
        ExamChromosome bestSolution = new ExamChromosome(population.get(0));
        int stagnationCount = 0;

        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            log.debug("Starting generation {}", generation + 1);
            population = evolvePopulation(population, classes, classToTeacherSubjects, teachers, rooms,
                    semesterId, schoolYearId, startDate, endDate, subjectExamInfos);
            Collections.sort(population, Comparator.comparingDouble(ExamChromosome::getFitness));
            ExamChromosome currentBest = population.get(0);

            if (currentBest.getFitness() < bestSolution.getFitness()) {
                bestSolution = new ExamChromosome(currentBest);
                stagnationCount = 0;
                log.debug("New best solution found with fitness: {}", bestSolution.getFitness());
            } else {
                stagnationCount++;
            }

            log.debug("Generation {}: Best fitness = {}", generation + 1, bestSolution.getFitness());

            if (stagnationCount >= STAGNATION_THRESHOLD) {
                log.info("Stagnation detected at generation {}. Restarting 50% of population.", generation + 1);
                population = restartPopulation(population, classes, classToTeacherSubjects, teachers, rooms,
                        semesterId, schoolYearId, startDate, endDate, subjectExamInfos);
                stagnationCount = 0;
            }

            if (bestSolution.getFitness() == 0) {
                log.info("Found optimal solution at generation {}", generation + 1);
                break;
            }

            ExamChromosome bestSolutionCopy = new ExamChromosome(bestSolution);
            bestSolutionCopy.localSearch(classes, teachers, classToTeacherSubjects, rooms,
                    semesterId, schoolYearId, startDate, endDate, subjectExamInfos);
            if (bestSolutionCopy.getFitness() < bestSolution.getFitness()) {
                bestSolution = bestSolutionCopy;
                log.debug("Local search improved bestSolution to fitness: {}", bestSolution.getFitness());
            }

            population.set(0, new ExamChromosome(bestSolution));
            Collections.sort(population, Comparator.comparingDouble(ExamChromosome::getFitness));
        }

        List<ExamSchedule> result = bestSolution.getExamSchedule();
        examScheduleRepo.saveAll(result);
        log.info("Exam schedule generation completed with {} exams saved", result.size());
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

    private List<ExamChromosome> initializePopulation(List<ClassResponse> classes,
                                                      Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects,
                                                      List<TeacherResponse> teachers, List<RoomResponse> rooms,
                                                      Long semesterId, Long schoolYearId, LocalDate startDate, LocalDate endDate,
                                                      List<SubjectExamInfo> subjectExamInfos) {
        List<ExamChromosome> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            ExamChromosome chromosome = new ExamChromosome();
            chromosome.assignExamSlots(classes, classToTeacherSubjects, teachers, rooms,
                    semesterId, schoolYearId, startDate, endDate, subjectExamInfos);
            chromosome.calculateFitness(classes, teachers, classToTeacherSubjects, rooms, subjectExamInfos);
            chromosome.repairConflicts(classes, teachers, classToTeacherSubjects, rooms,
                    semesterId, schoolYearId, startDate, endDate, subjectExamInfos);
            population.add(chromosome);
        }
        log.debug("Initialized population with {} chromosomes", population.size());
        return population;
    }

    private List<ExamChromosome> evolvePopulation(List<ExamChromosome> population, List<ClassResponse> classes,
                                                  Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects,
                                                  List<TeacherResponse> teachers, List<RoomResponse> rooms,
                                                  Long semesterId, Long schoolYearId, LocalDate startDate, LocalDate endDate,
                                                  List<SubjectExamInfo> subjectExamInfos) {
        List<ExamChromosome> newPopulation = new ArrayList<>();
        Collections.sort(population, Comparator.comparingDouble(ExamChromosome::getFitness));

        int eliteSize = (int) (POPULATION_SIZE * 0.1);
        newPopulation.addAll(population.subList(0, eliteSize));
        double bestParentFitness = population.get(0).getFitness();
        double fitnessTolerance = bestParentFitness * 1.5;

        Random rand = new Random();
        while (newPopulation.size() < POPULATION_SIZE) {
            ExamChromosome parent1 = tournamentSelection(population);
            ExamChromosome parent2 = tournamentSelection(population);
            ExamChromosome child = uniformCrossover(parent1, parent2, classes, teachers, classToTeacherSubjects, rooms,
                    semesterId, schoolYearId, startDate, endDate, subjectExamInfos);

            if (Math.random() < MUTATION_RATE) {
                child.mutate(classes, teachers, classToTeacherSubjects, rooms,
                        semesterId, schoolYearId, startDate, endDate, subjectExamInfos);
            }
            child.repairConflicts(classes, teachers, classToTeacherSubjects, rooms,
                    semesterId, schoolYearId, startDate, endDate, subjectExamInfos);
            child.calculateFitness(classes, teachers, classToTeacherSubjects, rooms, subjectExamInfos);

            if (child.getFitness() <= fitnessTolerance || child.getFitness() < bestParentFitness) {
                newPopulation.add(child);
            } else {
                newPopulation.add(parent1.getFitness() < parent2.getFitness() ? parent1 : parent2);
            }
        }

        return newPopulation.subList(0, POPULATION_SIZE);
    }

    private ExamChromosome tournamentSelection(List<ExamChromosome> population) {
        Random rand = new Random();
        ExamChromosome best = population.get(rand.nextInt(population.size()));
        for (int i = 1; i < TOURNAMENT_SIZE; i++) {
            ExamChromosome contender = population.get(rand.nextInt(population.size()));
            if (contender.getFitness() < best.getFitness()) {
                best = contender;
            }
        }
        return best;
    }

    private List<ExamChromosome> restartPopulation(List<ExamChromosome> population, List<ClassResponse> classes,
                                                   Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects,
                                                   List<TeacherResponse> teachers, List<RoomResponse> rooms,
                                                   Long semesterId, Long schoolYearId, LocalDate startDate, LocalDate endDate,
                                                   List<SubjectExamInfo> subjectExamInfos) {
        population.sort(Comparator.comparingDouble(ExamChromosome::getFitness));
        int keepSize = POPULATION_SIZE / 2;
        List<ExamChromosome> newPopulation = new ArrayList<>(population.subList(0, keepSize));

        for (int i = 0; i < POPULATION_SIZE - keepSize; i++) {
            ExamChromosome chromosome = new ExamChromosome();
            chromosome.assignExamSlots(classes, classToTeacherSubjects, teachers, rooms,
                    semesterId, schoolYearId, startDate, endDate, subjectExamInfos);
            chromosome.repairConflicts(classes, teachers, classToTeacherSubjects, rooms,
                    semesterId, schoolYearId, startDate, endDate, subjectExamInfos);
            chromosome.calculateFitness(classes, teachers, classToTeacherSubjects, rooms, subjectExamInfos);
            newPopulation.add(chromosome);
        }
        return newPopulation;
    }

    private ExamChromosome uniformCrossover(ExamChromosome parent1, ExamChromosome parent2, List<ClassResponse> classes,
                                            List<TeacherResponse> teachers, Map<ClassResponse, List<TeacherSubjectClassResponse>> classToTeacherSubjects,
                                            List<RoomResponse> rooms, Long semesterId, Long schoolYearId,
                                            LocalDate startDate, LocalDate endDate, List<SubjectExamInfo> subjectExamInfos) {
        ExamChromosome child = new ExamChromosome();
        Random rand = new Random();
        List<ExamSchedule> schedule1 = parent1.getExamSchedule();
        List<ExamSchedule> schedule2 = parent2.getExamSchedule();

        Map<String, ExamSchedule> uniqueSlots = new HashMap<>();
        double parent1Weight = parent1.getFitness() < parent2.getFitness() ? 0.7 : 0.5;

        Map<Long, List<ExamSchedule>> subjectToSlots = new HashMap<>();
        for (ExamSchedule slot : schedule1) {
            subjectToSlots.computeIfAbsent(slot.getSubjectId(), k -> new ArrayList<>()).add(slot);
        }
        for (ExamSchedule slot : schedule2) {
            subjectToSlots.computeIfAbsent(slot.getSubjectId(), k -> new ArrayList<>()).add(slot);
        }

        for (Map.Entry<Long, List<ExamSchedule>> entry : subjectToSlots.entrySet()) {
            Long subjectId = entry.getKey();
            List<ExamSchedule> slots = entry.getValue();
            ExamSchedule selectedSlot = slots.get(rand.nextInt(slots.size()));
            SubjectExamInfo examInfo = subjectExamInfos.stream()
                    .filter(info -> info.getSubjectId().equals(subjectId))
                    .findFirst()
                    .orElse(null);
            if (examInfo == null) continue;

            LocalDate examDate = selectedSlot.getExamDate();
            LocalTime examTime = selectedSlot.getExamTime();
            for (ClassResponse clazz : classes) {
                List<TeacherSubjectClassResponse> teacherSubjects = classToTeacherSubjects.getOrDefault(clazz, Collections.emptyList());
                SubjectResponse subject = teacherSubjects.stream()
                        .map(TeacherSubjectClassResponse::getSubject)
                        .filter(s -> s.getId().equals(subjectId))
                        .findFirst()
                        .orElse(null);
                if (subject == null) continue;

                TeacherResponse teacher = teacherSubjects.stream()
                        .filter(ts -> ts.getSubject().getId().equals(subjectId))
                        .map(TeacherSubjectClassResponse::getTeacher)
                        .findFirst()
                        .orElse(teachers.get(0));
                String teacherKey = teacher.getUserId() + "_" + examDate + "_" + examTime;
                String roomKey = clazz.getRoomResponse().getRoomId() + "_" + examDate + "_" + examTime;
                String classKey = clazz.getClassId() + "_" + subjectId;

                if (uniqueSlots.get(classKey) == null &&
                        child.getTeacherSlots().getOrDefault(teacherKey, new HashSet<>()).isEmpty() &&
                        child.getRoomSlots().getOrDefault(roomKey, new HashSet<>()).isEmpty() &&
                        child.isSlotValid(clazz, subject, examDate, examTime, teacher, clazz.getRoomResponse(), examInfo)) {
                    ExamSchedule newSlot = new ExamSchedule();
                    newSlot.setClassId(clazz.getClassId());
                    newSlot.setSubjectId(subjectId);
                    newSlot.setExamDate(examDate);
                    newSlot.setExamTime(examTime);
                    newSlot.setRoomId(clazz.getRoomResponse().getRoomId());
                    newSlot.setTeacherId(teacher.getUserId());
                    newSlot.setSemesterId(semesterId);
                    newSlot.setSchoolYearId(schoolYearId);
                    child.getExamSchedule().add(newSlot);
                    child.updateSlotMaps(newSlot);
                    uniqueSlots.put(classKey, newSlot);
                }
            }
        }

        child.repairConflicts(classes, teachers, classToTeacherSubjects, rooms,
                semesterId, schoolYearId, startDate, endDate, subjectExamInfos);
        return child;
    }

    public List<GetExamScheduleResponse> getExamSchedulesByClassId(Long classId) {
        LocalDate today = LocalDate.now();
        List<ExamSchedule> examSchedules = examScheduleRepo.findByClassIdAndExamDateBetween(classId, today.minusDays(15), today.plusDays(30));
        if (examSchedules == null || examSchedules.isEmpty()) {
            return Collections.emptyList();
        }

        List<GetExamScheduleResponse> response = new ArrayList<>();
        List<Long> subjectIds = examSchedules.stream()
                .map(ExamSchedule::getSubjectId)
                .distinct()
                .toList();
        Map<Long, SubjectExamInfo> subjectExamInfoMap = subjectExamInfoRepo.findBySubjectIdIn(subjectIds).stream()
                .collect(Collectors.toMap(SubjectExamInfo::getSubjectId, info -> info));
        Map<Long, SubjectResponse> subjectMap = subjectServiceClient.getSubjectByIds(subjectIds).getResult().stream()
                .collect(Collectors.toMap(SubjectResponse::getId, subject -> subject));
        List<String> teacherIds = examSchedules.stream()
                .map(ExamSchedule::getTeacherId)
                .distinct()
                .toList();
        Map<String, TeacherResponse> teacherMap = userServiceClient.getTeachersInfo(teacherIds).getResult().stream()
                .collect(Collectors.toMap(TeacherResponse::getUserId, teacher -> teacher));
        RoomResponse roomResponse = schoolServiceClient.getRoomById(examSchedules.get(0).getRoomId()).getResult();
        ClassResponse classResponse = schoolServiceClient.getClassById(classId).getResult();
        SemesterResponse semesterResponse = schoolServiceClient.getSemesterById(examSchedules.get(0).getSemesterId()).getResult();
        SchoolYearResponse schoolYearResponse = schoolServiceClient.getSchoolYearById(examSchedules.get(0).getSchoolYearId()).getResult();
        for (ExamSchedule examSchedule : examSchedules) {
            GetExamScheduleResponse scheduleResponse = new GetExamScheduleResponse();
            scheduleResponse.setId(examSchedule.getId());
            scheduleResponse.setClassId(examSchedule.getClassId());
            scheduleResponse.setClassName(classResponse != null ? classResponse.getClassName() : "Unknown Class");
            scheduleResponse.setSubjectId(examSchedule.getSubjectId());
            SubjectResponse subject = subjectMap.get(examSchedule.getSubjectId());
            scheduleResponse.setSubjectName(subject != null ? subject.getName() : "Unknown Subject");
            scheduleResponse.setExamDate(examSchedule.getExamDate());
            scheduleResponse.setExamTime(examSchedule.getExamTime());
            scheduleResponse.setRoomId(examSchedule.getRoomId());
            scheduleResponse.setRoomName(roomResponse != null ? roomResponse.getRoomName() : "Unknown Room");
            scheduleResponse.setTeacherId(examSchedule.getTeacherId());
            TeacherResponse teacher = teacherMap.get(examSchedule.getTeacherId());
            scheduleResponse.setTeacherName(teacher != null ? teacher.getFullName() : "Unknown Teacher");
            scheduleResponse.setSemesterId(examSchedule.getSemesterId());
            scheduleResponse.setSchoolYearId(examSchedule.getSchoolYearId());
            scheduleResponse.setDuration(subjectExamInfoMap.get(examSchedule.getSubjectId()) != null ?
                    subjectExamInfoMap.get(examSchedule.getSubjectId()).getDuration() : 0);
            scheduleResponse.setType(subjectExamInfoMap.get(examSchedule.getSubjectId()) != null ?
                    subjectExamInfoMap.get(examSchedule.getSubjectId()).getType() : SubjectExamInfo.ExamType.THEORY);

            response.add(scheduleResponse);
        }
        return response;
    }

    public List<GetExamScheduleResponse> getExamSchedulesByTeacherId(String teacherId) {
        LocalDate today = LocalDate.now();
        List<ExamSchedule> examSchedules = examScheduleRepo.findByTeacherIdAndExamDateBetween(teacherId, today.minusDays(15), today.plusDays(30));
        if (examSchedules == null || examSchedules.isEmpty()) {
            return Collections.emptyList();
        }
        List<GetExamScheduleResponse> response = new ArrayList<>();
        List<Long> subjectIds = examSchedules.stream()
                .map(ExamSchedule::getSubjectId)
                .distinct()
                .toList();
        Map<Long, SubjectExamInfo> subjectExamInfoMap = subjectExamInfoRepo.findBySubjectIdIn(subjectIds).stream()
                .collect(Collectors.toMap(SubjectExamInfo::getSubjectId, info -> info));
        Map<Long, SubjectResponse> subjectMap = subjectServiceClient.getSubjectByIds(subjectIds).getResult().stream()
                .collect(Collectors.toMap(SubjectResponse::getId, subject -> subject));
        List<Long> classIds = examSchedules.stream()
                .map(ExamSchedule::getClassId)
                .distinct()
                .toList();
        Map<Long, ClassResponse> classMap = schoolServiceClient.getClassByIds(classIds).getResult().stream()
                .collect(Collectors.toMap(ClassResponse::getClassId, clazz -> clazz));
        List<Long> roomIds = examSchedules.stream()
                .map(ExamSchedule::getRoomId)
                .distinct()
                .toList();
        Map<Long, RoomResponse> roomMap = schoolServiceClient.getRoomByIds(roomIds).getResult().stream()
                .collect(Collectors.toMap(RoomResponse::getRoomId, room -> room));
        return examSchedules.stream().map(examSchedule -> {
            GetExamScheduleResponse scheduleResponse = new GetExamScheduleResponse();
            scheduleResponse.setId(examSchedule.getId());
            scheduleResponse.setClassId(examSchedule.getClassId());
            ClassResponse classResponse = classMap.get(examSchedule.getClassId());
            scheduleResponse.setClassName(classResponse != null ? classResponse.getClassName() : "Unknown Class");
            scheduleResponse.setSubjectId(examSchedule.getSubjectId());
            SubjectResponse subject = subjectMap.get(examSchedule.getSubjectId());
            scheduleResponse.setSubjectName(subject != null ? subject.getName() : "Unknown Subject");
            scheduleResponse.setExamDate(examSchedule.getExamDate());
            scheduleResponse.setExamTime(examSchedule.getExamTime());
            scheduleResponse.setRoomId(examSchedule.getRoomId());
            RoomResponse room = roomMap.get(examSchedule.getRoomId());
            scheduleResponse.setRoomName(room != null ? room.getRoomName() : "Unknown Room");
            scheduleResponse.setTeacherId(examSchedule.getTeacherId());
            scheduleResponse.setTeacherName(teacherId);
            scheduleResponse.setSemesterId(examSchedule.getSemesterId());
            scheduleResponse.setSchoolYearId(examSchedule.getSchoolYearId());
            scheduleResponse.setDuration(subjectExamInfoMap.get(examSchedule.getSubjectId()) != null ?
                    subjectExamInfoMap.get(examSchedule.getSubjectId()).getDuration() : 0);
            scheduleResponse.setType(subjectExamInfoMap.get(examSchedule.getSubjectId()) != null ?
                    subjectExamInfoMap.get(examSchedule.getSubjectId()).getType() : SubjectExamInfo.ExamType.THEORY);
            return scheduleResponse;
        }).collect(Collectors.toList());
    }

    public List<GetExamScheduleResponse> getExamSchedulesByClassIdAndSemesterId(Long classId, Long semesterId) {
        List<ExamSchedule> examSchedules = examScheduleRepo.findByClassIdAndSemesterId(classId, semesterId);
        if (examSchedules == null || examSchedules.isEmpty()) {
            return Collections.emptyList();
        }
        List<GetExamScheduleResponse> response = new ArrayList<>();
        List<Long> subjectIds = examSchedules.stream()
                .map(ExamSchedule::getSubjectId)
                .distinct()
                .toList();
        Map<Long, SubjectExamInfo> subjectExamInfoMap = subjectExamInfoRepo.findBySubjectIdIn(subjectIds).stream()
                .collect(Collectors.toMap(SubjectExamInfo::getSubjectId, info -> info));
        Map<Long, SubjectResponse> subjectMap = subjectServiceClient.getSubjectByIds(subjectIds).getResult().stream()
                .collect(Collectors.toMap(SubjectResponse::getId, subject -> subject));
        List<String> teacherIds = examSchedules.stream()
                .map(ExamSchedule::getTeacherId)
                .distinct()
                .toList();
        Map<String, TeacherResponse> teacherMap = userServiceClient.getTeachersInfo(teacherIds).getResult().stream()
                .collect(Collectors.toMap(TeacherResponse::getUserId, teacher -> teacher));
        List<Long> roomIds = examSchedules.stream()
                .map(ExamSchedule::getRoomId)
                .distinct()
                .toList();
        Map<Long, RoomResponse> roomMap = schoolServiceClient.getRoomByIds(roomIds).getResult().stream()
                .collect(Collectors.toMap(RoomResponse::getRoomId, room -> room));
        ClassResponse classResponse = schoolServiceClient.getClassById(classId).getResult();
        SemesterResponse semesterResponse = schoolServiceClient.getSemesterById(semesterId).getResult();
        SchoolYearResponse schoolYearResponse = schoolServiceClient.getSchoolYearById(examSchedules.get(0).getSchoolYearId()).getResult();
        for (ExamSchedule examSchedule : examSchedules) {
            GetExamScheduleResponse scheduleResponse = new GetExamScheduleResponse();
            scheduleResponse.setId(examSchedule.getId());
            scheduleResponse.setClassId(examSchedule.getClassId());
            scheduleResponse.setClassName(classResponse != null ? classResponse.getClassName() : "Unknown Class");
            scheduleResponse.setSubjectId(examSchedule.getSubjectId());
            SubjectResponse subject = subjectMap.get(examSchedule.getSubjectId());
            scheduleResponse.setSubjectName(subject != null ? subject.getName() : "Unknown Subject");
            scheduleResponse.setExamDate(examSchedule.getExamDate());
            scheduleResponse.setExamTime(examSchedule.getExamTime());
            scheduleResponse.setRoomId(examSchedule.getRoomId());
            scheduleResponse.setRoomName(roomMap.get(examSchedule.getRoomId()) != null ? roomMap.get(examSchedule.getRoomId()).getRoomName() : "Unknown Room");
            scheduleResponse.setTeacherId(examSchedule.getTeacherId());
            TeacherResponse teacher = teacherMap.get(examSchedule.getTeacherId());
            scheduleResponse.setTeacherName(teacher != null ? teacher.getFullName() : "Unknown Teacher");
            scheduleResponse.setSemesterId(examSchedule.getSemesterId());
            scheduleResponse.setSchoolYearId(examSchedule.getSchoolYearId());
            scheduleResponse.setDuration(subjectExamInfoMap.get(examSchedule.getSubjectId()) != null ?
                    subjectExamInfoMap.get(examSchedule.getSubjectId()).getDuration() : 0);
            scheduleResponse.setType(subjectExamInfoMap.get(examSchedule.getSubjectId()) != null ?
                    subjectExamInfoMap.get(examSchedule.getSubjectId()).getType() : SubjectExamInfo.ExamType.THEORY);
            response.add(scheduleResponse);
        }
        return response;
    }

    public GetExamScheduleResponse createExamSchedule(ExamScheduleCreationRequest examSchedule) {
        if (examSchedule == null) {
            throw new IllegalArgumentException("Exam schedule request must not be null");
        }
        SubjectExamInfo subjectExamInfo = subjectExamInfoRepo.findBySubjectId(examSchedule.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid subject ID: " + examSchedule.getSubjectId()));
        String timeRequest = examSchedule.getExamTime();
        if (timeRequest == null || timeRequest.isEmpty()) {
            throw new IllegalArgumentException("Exam time must not be null or empty");
        }
        LocalTime examStartTime;
        try {
            examStartTime = LocalTime.parse(timeRequest, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid exam time format. Use HH:mm format.");
        }
        LocalTime examEndTime = examStartTime.plusMinutes(subjectExamInfo.getDuration());

        List<ExamSchedule> teacherSchedules = examScheduleRepo.findByTeacherIdAndExamDate(
                examSchedule.getTeacherId(), examSchedule.getExamDate());
        for (ExamSchedule existing : teacherSchedules) {
            SubjectExamInfo existingExamInfo = subjectExamInfoRepo.findBySubjectId(existing.getSubjectId())
                    .orElseThrow(() -> new IllegalStateException("Subject exam info not found for subject ID: " + existing.getSubjectId()));
            LocalTime existingStartTime = existing.getExamTime();
            LocalTime existingEndTime = existingStartTime.plusMinutes(existingExamInfo.getDuration());

            if (examStartTime.isBefore(existingEndTime) && examEndTime.isAfter(existingStartTime)) {
                log.error("Teacher {} is already assigned to another exam from {} to {} on {}",
                        examSchedule.getTeacherId(), existingStartTime, existingEndTime, examSchedule.getExamDate());
                throw new IllegalArgumentException(
                        String.format("Teacher %s is already assigned to another exam from %s to %s on %s",
                                examSchedule.getTeacherId(),
                                existingStartTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                existingEndTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                examSchedule.getExamDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            }
        }

        List<ExamSchedule> classSchedules = examScheduleRepo.findByClassIdAndExamDate(
                examSchedule.getClassId(), examSchedule.getExamDate());
        for (ExamSchedule existing : classSchedules) {
            SubjectExamInfo existingExamInfo = subjectExamInfoRepo.findBySubjectId(existing.getSubjectId())
                    .orElseThrow(() -> new IllegalStateException("Subject exam info not found for subject ID: " + existing.getSubjectId()));
            LocalTime existingStartTime = existing.getExamTime();
            LocalTime existingEndTime = existingStartTime.plusMinutes(existingExamInfo.getDuration());

            if (examStartTime.isBefore(existingEndTime) && examEndTime.isAfter(existingStartTime)) {
                log.error("Class {} is already scheduled for another exam from {} to {} on {}",
                        examSchedule.getClassId(), existingStartTime, existingEndTime, examSchedule.getExamDate());
                throw new IllegalArgumentException(
                        String.format("Class %s is already scheduled for another exam from %s to %s on %s",
                                examSchedule.getClassId(),
                                existingStartTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                existingEndTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                examSchedule.getExamDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            }
        }

        ExamSchedule newExamSchedule = ExamSchedule.builder()
                .classId(examSchedule.getClassId())
                .subjectId(examSchedule.getSubjectId())
                .examDate(examSchedule.getExamDate())
                .examTime(examStartTime)
                .roomId(examSchedule.getRoomId())
                .teacherId(examSchedule.getTeacherId())
                .semesterId(examSchedule.getSemesterId())
                .schoolYearId(examSchedule.getSchoolYearId())
                .build();

        newExamSchedule = examScheduleRepo.save(newExamSchedule);
        return mapToResponse(newExamSchedule);
    }

    public GetExamScheduleResponse mapToResponse(ExamSchedule examSchedule) {
        if (examSchedule == null) {
            return null;
        }
        TeacherResponse teacher = userServiceClient.getTeacherInfo(examSchedule.getTeacherId()).getResult();
        SubjectResponse subject = subjectServiceClient.getSubjectById(examSchedule.getSubjectId()).getResult();
        SubjectExamInfo subjectExamInfo = subjectExamInfoRepo.findBySubjectId(examSchedule.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid subject ID: " + examSchedule.getSubjectId()));
        ClassResponse classResponse = schoolServiceClient.getClassById(examSchedule.getClassId()).getResult();
        RoomResponse roomResponse = schoolServiceClient.getRoomById(examSchedule.getRoomId()).getResult();

        return GetExamScheduleResponse.builder()
                .id(examSchedule.getId())
                .classId(examSchedule.getClassId())
                .subjectId(examSchedule.getSubjectId())
                .examDate(examSchedule.getExamDate())
                .examTime(examSchedule.getExamTime())
                .roomId(examSchedule.getRoomId())
                .teacherId(examSchedule.getTeacherId())
                .semesterId(examSchedule.getSemesterId())
                .schoolYearId(examSchedule.getSchoolYearId())
                .className(classResponse != null ? classResponse.getClassName() : "Unknown Class")
                .subjectName(subject != null ? subject.getName() : "Unknown Subject")
                .roomName(roomResponse != null ? roomResponse.getRoomName() : "Unknown Room")
                .teacherName(teacher != null ? teacher.getFullName() : "Unknown Teacher")
                .duration(subjectExamInfo != null ? subjectExamInfo.getDuration() : 0)
                .type(subjectExamInfo != null ? subjectExamInfo.getType() : SubjectExamInfo.ExamType.THEORY)
                .build();
    }

    public GetExamScheduleResponse updateExamSchedule(Long id, ExamScheduleCreationRequest examSchedule) {
        if (id == null || examSchedule == null) {
            throw new IllegalArgumentException("Exam schedule ID and request must not be null");
        }

        SubjectExamInfo subjectExamInfo = subjectExamInfoRepo.findBySubjectId(examSchedule.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid subject ID: " + examSchedule.getSubjectId()));
        String timeRequest = examSchedule.getExamTime();
        if (timeRequest == null || timeRequest.isEmpty()) {
            throw new IllegalArgumentException("Exam time must not be null or empty");
        }
        LocalTime examStartTime;
        try {
            examStartTime = LocalTime.parse(timeRequest, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid exam time format. Use HH:mm format.");
        }
        LocalTime examEndTime = examStartTime.plusMinutes(subjectExamInfo.getDuration());

        List<ExamSchedule> teacherSchedules = examScheduleRepo.findByTeacherIdAndExamDate(
                examSchedule.getTeacherId(), examSchedule.getExamDate());
        for (ExamSchedule existing : teacherSchedules) {
            if (existing.getId().equals(id)) {
                continue;
            }
            SubjectExamInfo existingExamInfo = subjectExamInfoRepo.findBySubjectId(existing.getSubjectId())
                    .orElseThrow(() -> new IllegalStateException("Subject exam info not found for subject ID: " + existing.getSubjectId()));
            LocalTime existingStartTime = existing.getExamTime();
            LocalTime existingEndTime = existingStartTime.plusMinutes(existingExamInfo.getDuration());

            if (examStartTime.isBefore(existingEndTime) && examEndTime.isAfter(existingStartTime)) {
                throw new IllegalArgumentException(
                        String.format("Teacher %s is already assigned to another exam from %s to %s on %s",
                                examSchedule.getTeacherId(),
                                existingStartTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                existingEndTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                examSchedule.getExamDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            }
        }

        List<ExamSchedule> classSchedules = examScheduleRepo.findByClassIdAndExamDate(
                examSchedule.getClassId(), examSchedule.getExamDate());
        for (ExamSchedule existing : classSchedules) {
            if (existing.getId().equals(id)) {
                continue;
            }
            SubjectExamInfo existingExamInfo = subjectExamInfoRepo.findBySubjectId(existing.getSubjectId())
                    .orElseThrow(() -> new IllegalStateException("Subject exam info not found for subject ID: " + existing.getSubjectId()));
            LocalTime existingStartTime = existing.getExamTime();
            LocalTime existingEndTime = existingStartTime.plusMinutes(existingExamInfo.getDuration());

            if (examStartTime.isBefore(existingEndTime) && examEndTime.isAfter(existingStartTime)) {
                throw new IllegalArgumentException(
                        String.format("Class %s is already scheduled for another exam from %s to %s on %s",
                                examSchedule.getClassId(),
                                existingStartTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                existingEndTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                examSchedule.getExamDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            }
        }

        ExamSchedule existingExamSchedule = examScheduleRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid exam schedule ID"));

        existingExamSchedule.setClassId(examSchedule.getClassId());
        existingExamSchedule.setSubjectId(examSchedule.getSubjectId());
        existingExamSchedule.setExamDate(examSchedule.getExamDate());
        existingExamSchedule.setExamTime(examStartTime);
        existingExamSchedule.setRoomId(examSchedule.getRoomId());
        existingExamSchedule.setTeacherId(examSchedule.getTeacherId());
        existingExamSchedule.setSemesterId(examSchedule.getSemesterId());
        existingExamSchedule.setSchoolYearId(examSchedule.getSchoolYearId());

        existingExamSchedule = examScheduleRepo.save(existingExamSchedule);
        return mapToResponse(existingExamSchedule);
    }

    public void deleteExamSchedule(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Exam schedule ID must not be null");
        }

        ExamSchedule existingExamSchedule = examScheduleRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid exam schedule ID"));

        examScheduleRepo.delete(existingExamSchedule);
    }

    public byte[] exportExamScheduleToExcel(Long classId, Long semesterId) {
        log.info("Exporting exam schedule to Excel for classId: {}, semesterId: {}", classId, semesterId);
        List<GetExamScheduleResponse> examSchedules = getExamSchedulesByClassIdAndSemesterId(classId, semesterId);
        if (examSchedules.isEmpty()) {
            log.warn("No exam schedules found for classId: {}, semesterId: {}", classId, semesterId);
            throw new IllegalStateException("No exam schedules found for the specified class and semester");
        }

        ClassResponse clazz = schoolServiceClient.getClassById(classId).getResult();
        String className = clazz != null ? clazz.getClassName() : "Unknown Class";
        SemesterResponse semester = schoolServiceClient.getSemesterById(semesterId).getResult();
        String semesterName = semester != null ? semester.getSemesterName() : "Unknown Semester";

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Exam Schedule");

        CellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        wrapStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        wrapStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Exam Schedule");
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        Row metaRow1 = sheet.createRow(1);
        metaRow1.createCell(0).setCellValue("Class: " + className);
        Row metaRow2 = sheet.createRow(2);
        metaRow2.createCell(0).setCellValue("Semester: " + semesterName);
        Row metaRow3 = sheet.createRow(3);
        metaRow3.createCell(0).setCellValue("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        Row headerRow = sheet.createRow(5);
        String[] headers = {"No.", "Subject", "Date", "Time", "Room", "Supervisor", "Duration (mins)", "Type"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        headerRow.setHeight((short) (20 * 20)); // 20 points

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        for (int i = 0; i < examSchedules.size(); i++) {
            GetExamScheduleResponse exam = examSchedules.get(i);
            Row row = sheet.createRow(i + 6);
            row.setHeight((short) (60 * 20)); // 60 points for readability

            Cell cell0 = row.createCell(0);
            cell0.setCellValue(i + 1);
            cell0.setCellStyle(wrapStyle);

            Cell cell1 = row.createCell(1);
            cell1.setCellValue(exam.getSubjectName() != null ? exam.getSubjectName() : "N/A");
            cell1.setCellStyle(wrapStyle);

            Cell cell2 = row.createCell(2);
            cell2.setCellValue(exam.getExamDate() != null ? exam.getExamDate().format(dateFormatter) : "N/A");
            cell2.setCellStyle(wrapStyle);

            Cell cell3 = row.createCell(3);
            cell3.setCellValue(exam.getExamTime() != null ? exam.getExamTime().format(timeFormatter) : "N/A");
            cell3.setCellStyle(wrapStyle);

            Cell cell4 = row.createCell(4);
            cell4.setCellValue(exam.getRoomName() != null ? exam.getRoomName() : "N/A");
            cell4.setCellStyle(wrapStyle);

            Cell cell5 = row.createCell(5);
            cell5.setCellValue(exam.getTeacherName() != null ? exam.getTeacherName() : "N/A");
            cell5.setCellStyle(wrapStyle);

            Cell cell6 = row.createCell(6);
            cell6.setCellValue(exam.getDuration() > 0 ? exam.getDuration() : 0);
            cell6.setCellStyle(wrapStyle);

            Cell cell7 = row.createCell(7);
            cell7.setCellValue(exam.getType() != null ? exam.getType().toString() : "N/A");
            cell7.setCellStyle(wrapStyle);
        }

        for (int i = 0; i < 8; i++) {
            sheet.autoSizeColumn(i);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            workbook.write(baos);
            workbook.close();
            log.info("Exported exam schedule Excel for classId: {}, semesterId: {}", classId, semesterId);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to export exam schedule Excel: {}", e.getMessage());
            throw new RuntimeException("Failed to export exam schedule to Excel", e);
        }
    }

    public byte[] generateExamScheduleToPDF(Long semesterId, Long classId) {
        log.info("Generating exam schedule PDF for classId: {}, semesterId: {}", classId, semesterId);
        List<GetExamScheduleResponse> examSchedules = getExamSchedulesByClassIdAndSemesterId(classId, semesterId);
        if (examSchedules.isEmpty()) {
            log.warn("No exam schedules found for classId: {}, semesterId: {}", classId, semesterId);
            throw new IllegalStateException("No exam schedules found for the specified class and semester");
        }

        ClassResponse clazz = schoolServiceClient.getClassById(classId).getResult();
        String className = clazz != null ? clazz.getClassName() : "Unknown Class";
        SemesterResponse semester = schoolServiceClient.getSemesterById(semesterId).getResult();
        String semesterName = semester != null ? semester.getSemesterName() : "Unknown Semester";

        Document document = new Document(PageSize.A4); // Portrait orientation
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            BaseFont baseFont;
            com.itextpdf.text.Font font;
            com.itextpdf.text.Font headerFont;
            try {
                if (getClass().getResource("/fonts/NotoSans-Regular.ttf") == null) {
                    throw new IOException("Font file /fonts/NotoSans-Regular.ttf not found in classpath");
                }
                baseFont = BaseFont.createFont("fonts/NotoSans-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                font = new com.itextpdf.text.Font(baseFont, 8);
                headerFont = new com.itextpdf.text.Font(baseFont, 12, com.itextpdf.text.Font.BOLD);
            } catch (Exception e) {
                log.warn("Failed to load NotoSans-Regular.ttf: {}. Falling back to Helvetica.", e.getMessage());
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
                font = new com.itextpdf.text.Font(baseFont, 10);
                headerFont = new com.itextpdf.text.Font(baseFont, 12, com.itextpdf.text.Font.BOLD);
            }

            Paragraph title = new Paragraph("Exam Schedule", headerFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("Class: " + className, font));
            document.add(new Paragraph("Semester: " + semesterName, font));
            document.add(new Paragraph("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), font));
            document.add(new Paragraph(" ")); // Spacer

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.5f, 2f, 1.5f, 1f, 1.5f, 2f, 2.5f, 3f}); // Adjust column widths

            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerCell.setPadding(5);
            String[] headers = {"No.", "Subject", "Date", "Time", "Room", "Supervisor", "Duration (mins)", "Type"};
            for (String header : headers) {
                headerCell.setPhrase(new Phrase(header, headerFont));
                table.addCell(headerCell);
            }

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            for (int i = 0; i < examSchedules.size(); i++) {
                GetExamScheduleResponse exam = examSchedules.get(i);
                PdfPCell cell = new PdfPCell();
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(5);
                cell.setFixedHeight(40); // ~3cm for readability

                cell.setPhrase(new Phrase(String.valueOf(i + 1), font));
                table.addCell(cell);
                cell.setPhrase(new Phrase(exam.getSubjectName() != null ? exam.getSubjectName() : "N/A", font));
                table.addCell(cell);
                cell.setPhrase(new Phrase(exam.getExamDate() != null ? exam.getExamDate().format(dateFormatter) : "N/A", font));
                table.addCell(cell);
                cell.setPhrase(new Phrase(exam.getExamTime() != null ? exam.getExamTime().format(timeFormatter) : "N/A", font));
                table.addCell(cell);
                cell.setPhrase(new Phrase(exam.getRoomName() != null ? exam.getRoomName() : "N/A", font));
                table.addCell(cell);
                cell.setPhrase(new Phrase(exam.getTeacherName() != null ? exam.getTeacherName() : "N/A", font));
                table.addCell(cell);
                cell.setPhrase(new Phrase(exam.getDuration() > 0 ? String.valueOf(exam.getDuration()) : "0", font));
                table.addCell(cell);
                cell.setPhrase(new Phrase(exam.getType() != null ? exam.getType().toString() : "N/A", font));
                table.addCell(cell);
            }

            document.add(table);
            document.close();
            log.info("Generated exam schedule PDF for classId: {}, semesterId: {}", classId, semesterId);
            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("Failed to generate exam schedule PDF: {}", e.getMessage());
            throw new RuntimeException("Failed to generate exam schedule PDF", e);
        }
    }
}