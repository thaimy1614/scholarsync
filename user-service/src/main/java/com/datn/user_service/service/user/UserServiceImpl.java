package com.datn.user_service.service.user;

import com.datn.user_service.dto.request.RegisterParentRequest;
import com.datn.user_service.dto.request.RegisterStudentRequest;
import com.datn.user_service.dto.request.RegisterTeacherRequest;
import com.datn.user_service.dto.request.RegisterUser;
import com.datn.user_service.dto.response.*;
import com.datn.user_service.exception.AppException;
import com.datn.user_service.exception.ErrorCode;
import com.datn.user_service.mapper.ParentMapper;
import com.datn.user_service.mapper.StudentMapper;
import com.datn.user_service.mapper.TeacherMapper;
import com.datn.user_service.mapper.UserMapper;
import com.datn.user_service.model.*;
import com.datn.user_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final ParentMapper parentMapper;
    private final UserMapper userMapper;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ParentRepository parentRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final ParentStudentRepository parentStudentRepository;

    public Page<UserResponse> getAllUsers(int page, int size, String sortBy, String direction) {
        Pageable pageable = getPageable(page, size, sortBy, direction);
        Page<User> users = userRepository.findAll(pageable);
        return users.map(userMapper::toUserResponse);
    }

    public Page<StudentResponse> getAllStudents(int page, int size, String sortBy, String direction) {
        Pageable pageable = getPageable(page, size, sortBy, direction);
        return studentRepository.findAll(pageable)
                .map(this::mapToStudentResponse);
    }

    public Page<TeacherResponse> getAllTeachers(int page, int size, String sortBy, String direction) {
        Pageable pageable = getPageable(page, size, sortBy, direction);
        return teacherRepository.findAll(pageable)
                .map(this::mapToTeacherResponse);
    }

    public Page<ParentResponse> getAllParents(int page, int size, String sortBy, String direction) {
        Pageable pageable = getPageable(page, size, sortBy, direction);
        return parentRepository.findAll(pageable)
                .map(this::mapToParentResponse);
    }

    public Pageable getPageable(int page, int size, String sortBy, String direction) {
        return PageRequest.of(page, size, direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
    }

    private StudentResponse mapToStudentResponse(Student student) {
        List<ParentStudent> parentStudents = parentStudentRepository.findAllByStudent(student);
        List<ParentResponse> parents = parentStudents.stream()
                .map(ParentStudent::getParent)
                .map(this::mapToSimpleParent)
                .toList();
        return StudentResponse.builder()
                .userId(student.getUserId())
                .fullName(student.getFullName())
                .email(student.getEmail())
                .phoneNumber(student.getPhoneNumber())
                .address(student.getAddress())
                .gender(student.getGender())
                .dateOfBirth(student.getDateOfBirth())
                .image(student.getImage())
                .createdAt(student.getCreateAt())
                .enrollmentYear(student.getEnrollmentYear())
                .status(student.getStatus())
                .parents(parents)
                .build();
    }

    private ParentResponse mapToSimpleParent(Parent parent) {
        return ParentResponse.builder()
                .userId(parent.getUserId())
                .fullName(parent.getFullName())
                .email(parent.getEmail())
                .phoneNumber(parent.getPhoneNumber())
                .address(parent.getAddress())
                .gender(parent.getGender())
                .dateOfBirth(parent.getDateOfBirth())
                .image(parent.getImage())
                .createdAt(parent.getCreateAt())
                .isNotificationOn(parent.getIsNotificationOn())
                .enrollmentYear(parent.getEnrollmentYear())
                .build();
    }

    private TeacherResponse mapToTeacherResponse(Teacher teacher) {
        return TeacherResponse.builder()
                .userId(teacher.getUserId())
                .fullName(teacher.getFullName())
                .email(teacher.getEmail())
                .phoneNumber(teacher.getPhoneNumber())
                .address(teacher.getAddress())
                .gender(teacher.getGender())
                .dateOfBirth(teacher.getDateOfBirth())
                .image(teacher.getImage())
                .createdAt(teacher.getCreateAt())
                .specialization(teacher.getSpecialization())
                .enrollmentYear(teacher.getEnrollmentYear())
                .yearsOfExperience(teacher.getYearsOfExperience())
                .degree(teacher.getDegree())
                .status(teacher.getStatus())
                .build();
    }

    private ParentResponse mapToParentResponse(Parent parent) {
        List<ParentStudent> parentStudents = parent.getStudents();
        List<StudentResponse> students = parentStudents.stream()
                .map(ParentStudent::getStudent)
                .map(this::mapToStudentResponse)
                .toList();
        return ParentResponse.builder()
                .userId(parent.getUserId())
                .fullName(parent.getFullName())
                .email(parent.getEmail())
                .phoneNumber(parent.getPhoneNumber())
                .address(parent.getAddress())
                .gender(parent.getGender())
                .dateOfBirth(parent.getDateOfBirth())
                .image(parent.getImage())
                .createdAt(parent.getCreateAt())
                .isNotificationOn(parent.getIsNotificationOn())
                .students(students)
                .enrollmentYear(parent.getEnrollmentYear())
                .build();
    }

    public Page<?> searchUsers(String keyword, String role, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        boolean isEmailSearch = keyword.contains("@");
        String searchKey = isEmailSearch ? keyword.replace("@", "") : keyword;

        if (role == null || role.trim().isEmpty()) {

            Page<Student> students = isEmailSearch
                    ? studentRepository.findByEmailContainingIgnoreCase(searchKey, pageable)
                    : studentRepository.findByFullNameContainingIgnoreCase(searchKey, pageable);
            List<Object> allUsers = new ArrayList<>(students.getContent().stream()
                    .map(this::mapToStudentResponse)
                    .toList());

            Page<Teacher> teachers = isEmailSearch
                    ? teacherRepository.findByEmailContainingIgnoreCase(searchKey, pageable)
                    : teacherRepository.findByFullNameContainingIgnoreCase(searchKey, pageable);
            allUsers.addAll(teachers.getContent().stream()
                    .map(this::mapToTeacherResponse)
                    .toList());

            Page<Parent> parents = isEmailSearch
                    ? parentRepository.findByEmailContainingIgnoreCase(searchKey, pageable)
                    : parentRepository.findByFullNameContainingIgnoreCase(searchKey, pageable);
            allUsers.addAll(parents.getContent().stream()
                    .map(this::mapToParentResponse)
                    .toList());

            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), allUsers.size());
            List<Object> pagedUsers = start < end ? allUsers.subList(start, end) : new ArrayList<>();
            return new PageImpl<>(pagedUsers, pageable, allUsers.size());
        }

        return switch (role.toUpperCase()) {
            case "STUDENT" -> isEmailSearch
                    ? studentRepository.findByEmailContainingIgnoreCase(searchKey, pageable)
                    .map(this::mapToStudentResponse)
                    : studentRepository.findByFullNameContainingIgnoreCase(searchKey, pageable)
                    .map(this::mapToStudentResponse);
            case "TEACHER" -> isEmailSearch
                    ? teacherRepository.findByEmailContainingIgnoreCase(searchKey, pageable)
                    .map(this::mapToTeacherResponse)
                    : teacherRepository.findByFullNameContainingIgnoreCase(searchKey, pageable)
                    .map(this::mapToTeacherResponse);
            case "PARENT" -> isEmailSearch
                    ? parentRepository.findByEmailContainingIgnoreCase(searchKey, pageable)
                    .map(this::mapToParentResponse)
                    : parentRepository.findByFullNameContainingIgnoreCase(searchKey, pageable)
                    .map(this::mapToParentResponse);
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        };
    }

    public List<GetUserFullNameResponse> getFullName(List<String> userId) {
        List<User> users = userRepository.findAllById(userId);

        return users.stream().map(user -> GetUserFullNameResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .build()).collect(Collectors.toList());
    }


    public Object getMyInfo(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (user instanceof Student student) {
            return mapToStudentResponse(student);
        } else if (user instanceof Teacher teacher) {
            return mapToTeacherResponse(teacher);
        } else if (user instanceof Parent parent) {
            return mapToParentResponse(parent);
        } else {
            throw new AppException(ErrorCode.INVALID_KEY);
        }
    }

    public Object updateMyInfo(String userId, RegisterUser request) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        Object updatedUser;

        if (request instanceof RegisterStudentRequest studentRequest) {
            updatedUser = updateStudent(userId, studentRequest);

        } else if (request instanceof RegisterTeacherRequest teacherRequest) {
            updatedUser = updateTeacher(userId, teacherRequest);

        } else if (request instanceof RegisterParentRequest parentRequest) {
            updatedUser = updateParent(userId, parentRequest);

        } else {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        return updatedUser;
    }


    private StudentResponse updateStudent(String userId, RegisterStudentRequest studentRequest) {
        Student student = studentMapper.toStudent(studentRequest);
        student.setUserId(userId);

        Student savedStudent = studentRepository.save(student);
        return mapToStudentResponse(savedStudent);
    }

    private TeacherResponse updateTeacher(String userId, RegisterTeacherRequest teacherRequest) {
            Teacher teacher = teacherMapper.toTeacher(teacherRequest);
            teacher.setUserId(userId);

            Teacher savedTeacher = teacherRepository.save(teacher);
            return mapToTeacherResponse(savedTeacher);

    }

    private ParentResponse updateParent(String userId, RegisterParentRequest parentRequest) {
        Parent parent = parentMapper.toParent(parentRequest);
        parent.setUserId(userId);

        Parent savedParent = parentRepository.save(parent);
        return mapToParentResponse(savedParent);
    }

    public CountUserResponse countUsers() {
        Long numberOfStudents = studentRepository.count();
        Long numberOfTeachers = teacherRepository.count();
        Long numberOfParents = parentRepository.count();
        return CountUserResponse.builder()
                .numberOfStudents(numberOfStudents)
                .numberOfTeachers(numberOfTeachers)
                .numberOfParents(numberOfParents)
                .total(numberOfStudents + numberOfTeachers + numberOfParents)
                .build();
    }

    public List<Object> getUsersByIds(List<String> ids) {
        List<Student> students = studentRepository.findAllById(ids);
        List<Teacher> teachers = teacherRepository.findAllById(ids);
        List<Parent> parents = parentRepository.findAllById(ids);

        Map<String, Object> userMap = new LinkedHashMap<>();

        students.forEach(student -> userMap.put(student.getUserId(), mapToStudentResponse(student)));

        teachers.forEach(teacher -> userMap.put(teacher.getUserId(), mapToTeacherResponse(teacher)));

        parents.forEach(parent -> userMap.put(parent.getUserId(), mapToParentResponse(parent)));

        List<Object> result = new ArrayList<>();
        for (String id : ids) {
            if (userMap.containsKey(id)) {
                result.add(userMap.get(id));
            }
        }

        return result;
    }

    @Override
    public void blockUsers(List<String> ids) {
        List<Account> accounts = accountRepository.findAllById(ids);
        for (Account account : accounts) {
            account.setStatus(Account.Status.BLOCKED);
        }
        accountRepository.saveAll(accounts);
        //TODO: Send email to notify user
    }

    @Override
    public void unblockUsers(List<String> ids) {
        List<Account> accounts = accountRepository.findAllById(ids);
        for (Account account : accounts) {
            account.setStatus(Account.Status.ACTIVATED);
        }
        accountRepository.saveAll(accounts);
        //TODO: Send email to notify user
    }

    @Override
    public void deleteUsers(List<String> ids) {
        List<Account> accounts = accountRepository.findAllById(ids);
        for (Account account : accounts) {
            account.setStatus(Account.Status.DELETED);
        }
        accountRepository.saveAll(accounts);
    }

    public List<StudentResponse> getStudentsByIds(List<String> ids) {
        return studentRepository.findAllById(ids).stream()
                .map(this::mapToStudentResponse)
                .collect(Collectors.toList());
    }

    public List<TeacherResponse> getTeachersByIds(List<String> ids) {
        return teacherRepository.findAllById(ids).stream()
                .map(this::mapToTeacherResponse)
                .collect(Collectors.toList());
    }

    public List<ParentResponse> getParentsByIds(List<String> ids) {
        return parentRepository.findAllById(ids).stream()
                .map(this::mapToParentResponse)
                .collect(Collectors.toList());
    }

    public List<TeacherResponse> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(this::mapToTeacherResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TeacherResponse getTeacherInfo(String userId) {
        Teacher teacher = teacherRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return mapToTeacherResponse(teacher);
    }

    @Override
    public List<TeacherResponse> getTeachersInfo(List<String> ids) {
        List<Teacher> teachers = teacherRepository.findAllById(ids);
        return teachers.stream()
                .map(this::mapToTeacherResponse)
                .collect(Collectors.toList());
    }

    public StudentResponse setDiscipleMonitor(String userId) {
        Student student = studentRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_EXISTED));
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (account.getStatus() != Account.Status.ACTIVATED) {
            throw new AppException(ErrorCode.USER_NOT_ACTIVATED);
        }
        account.getRoles().stream()
                .filter(r -> r.getName().equalsIgnoreCase("STUDENT"))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_STUDENT));
        Set<Role> roles = account.getRoles();
        roles.add(roleRepository.findById("MONITOR")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED)));
        account.setRoles(roles);
        accountRepository.save(account);
        return mapToStudentResponse(student);
    }

    public StudentResponse getStudentInfo(String userId) {
        Student student = studentRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_EXISTED));
        List<ParentStudent> parentStudents = student.getParents();
        List<ParentResponse> parents = parentStudents.stream()
                .map(ParentStudent::getParent)
                .map(this::mapToParentResponse)
                .toList();
        return StudentResponse.builder()
                .userId(student.getUserId())
                .fullName(student.getFullName())
                .email(student.getEmail())
                .phoneNumber(student.getPhoneNumber())
                .address(student.getAddress())
                .gender(student.getGender())
                .dateOfBirth(student.getDateOfBirth())
                .image(student.getImage())
                .createdAt(student.getCreateAt())
                .enrollmentYear(student.getEnrollmentYear())
                .status(student.getStatus())
                .parents(parents)
                .build();
    }

    public Page<TeacherResponse> getAllPrincipals(int page, int size, String sortBy, String direction) {
        Pageable teacherPageable = getPageable(0, 10000, sortBy, direction);
        Pageable pageable = getPageable(page, size, sortBy, direction);
        Page<Teacher> allTeachers = teacherRepository.findAll(teacherPageable);

        if (allTeachers.isEmpty()) {
            return Page.empty(pageable);
        }

        List<String> userIds = allTeachers.stream()
                .map(Teacher::getUserId)
                .toList();

        List<Account> accounts = accountRepository.findAllById(userIds);
        Role principalRole = roleRepository.findById("PRINCIPAL")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        Map<String, Account> accountMap = accounts.stream()
                .collect(Collectors.toMap(Account::getUserId, Function.identity()));

        List<TeacherResponse> filtered = allTeachers.stream()
                .filter(t -> {
                    Account acc = accountMap.get(t.getUserId());
                    return acc != null && acc.getRoles().contains(principalRole);
                })
                .map(this::mapToTeacherResponse)
                .toList();

        return new PageImpl<>(filtered, pageable, filtered.size());
    }

    @Override
    public Page<StudentResponse> getAllMonitor(int page, int size, String sortBy, String direction) {
        Pageable studentPageable = getPageable(0, 10000, sortBy, direction);
        Pageable pageable = getPageable(page, size, sortBy, direction);
        Page<Student> allStudents = studentRepository.findAll(studentPageable);
        if (allStudents.isEmpty()) {
            return Page.empty(pageable);
        }
        List<String> userIds = allStudents.stream()
                .map(Student::getUserId)
                .toList();
        List<Account> accounts = accountRepository.findAllById(userIds);
        Role monitorRole = roleRepository.findById("MONITOR")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        Map<String, Account> accountMap = accounts.stream()
                .collect(Collectors.toMap(Account::getUserId, Function.identity()));
        List<StudentResponse> filtered = allStudents.stream()
                .filter(s -> {
                    Account acc = accountMap.get(s.getUserId());
                    return acc != null && acc.getRoles().contains(monitorRole);
                })
                .map(this::mapToStudentResponse)
                .toList();
        return new PageImpl<>(filtered, pageable, filtered.size());
    }
}

