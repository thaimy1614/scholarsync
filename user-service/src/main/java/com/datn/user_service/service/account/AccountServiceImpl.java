package com.datn.user_service.service.account;

import com.datn.user_service.dto.client.PresignedUrlRequest;
import com.datn.user_service.dto.client.PresignedUrlResponse;
import com.datn.user_service.dto.kafka.AccountInfo;
import com.datn.user_service.dto.kafka.SendOTP;
import com.datn.user_service.dto.kafka.SendPassword;
import com.datn.user_service.dto.kafka.VerifyAccount;
import com.datn.user_service.dto.request.*;
import com.datn.user_service.dto.response.*;
import com.datn.user_service.exception.AppException;
import com.datn.user_service.exception.ErrorCode;
import com.datn.user_service.httpclient.OutboundIdentityClient;
import com.datn.user_service.httpclient.OutboundUserClient;
import com.datn.user_service.httpclient.ResourceServiceClient;
import com.datn.user_service.mapper.ParentMapper;
import com.datn.user_service.mapper.StudentMapper;
import com.datn.user_service.mapper.TeacherMapper;
import com.datn.user_service.model.*;
import com.datn.user_service.repository.*;
import com.datn.user_service.service.BridgeService;
import com.datn.user_service.util.CustomMultipartFile;
import com.datn.user_service.util.StudentIdGenerator;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {
    @NonFinal
    protected final String GRANT_TYPE = "authorization_code";
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final OutboundIdentityClient outboundIdentityClient;
    private final OutboundUserClient outboundUserClient;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final ParentMapper parentMapper;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ParentRepository parentRepository;
    private final RoleRepository roleRepository;
    private final BridgeService bridgeService;
    private final ParentStudentRepository parentStudentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserRepository userRepository;
    private final StudentIdGenerator studentIdGenerator;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final ResourceServiceClient resourceServiceClient;
    private final RestClient restClient;
    @NonFinal
    @Value("${outbound.identity.client-id}")
    protected String CLIENT_ID;
    @NonFinal
    @Value("${outbound.identity.client-secret}")
    protected String CLIENT_SECRET;
    @NonFinal
    @Value("${outbound.identity.redirect-uri}")
    protected String REDIRECT_URI;
    @Value("${application.api.url}")
    private String apiUrl;
    @Value("${jwt.signer-key}")
    private String KEY;
    //    @Value("${jwt.io-stream-key}")
//    private String IO_STREAM_KEY;
//    @Value("${jwt.io-stream-secret}")
//    private String IO_STREAM_SECRET;
    @Value("${jwt.expiration-duration}")
    private long EXPIRATION_DURATION;
    @Value("${jwt.refreshable-duration}")
    private String REFRESHABLE_DURATION;
    @Value("${application.verify-redirect}")
    private String VERIFY_EMAIL_REDIRECT;

    public String generateUserId(String role, Integer enrollmentYear) {
        String prefix;
        switch (role.toUpperCase()) {
            case "STUDENT" -> prefix = "ST";
            case "TEACHER" -> prefix = "TE";
            case "PARENT" -> prefix = "PA";
            case "PRINCIPAL" -> prefix = "PR";
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        }

        int year = (enrollmentYear != null) ? enrollmentYear : LocalDate.now().getYear();

        Long sequenceValue = studentIdGenerator.getNextUserId(year, role);

        return prefix + sequenceValue;
    }


    public LoginResponse authenticate(LoginRequest request) throws JOSEException, MalformedURLException {
        if(!roleRepository.existsById(request.getRole().toUpperCase())){
            throw new AppException(ErrorCode.ROLE_NOT_EXISTED);
        }
        String username = request.getUsername();
        String password = request.getPassword();

        Account authUser = accountRepository.findByUsername(username).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean check = passwordEncoder.matches(password, authUser.getPassword());
        if (!check) {
            throw new AppException(ErrorCode.USERNAME_OR_PASSWORD_INCORRECT);
        }

        User user = userRepository.findById(authUser.getUserId()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (authUser.getStatus().equals(Account.Status.BLOCKED)) {
            throw new AppException(ErrorCode.ACCOUNT_BLOCKED);
        }

        if (authUser.getStatus().equals(Account.Status.UNVERIFIED)) {
            sendVerificationEmail(authUser.getEmail(), user.getFullName());
            throw new AppException(ErrorCode.UnverifiedAccount);
        }
        Set<String> roles = authUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        if (!roles.contains(request.getRole().toUpperCase())) {
            throw new AppException(ErrorCode.USER_NOT_HAVE_PERMISSION);
        }
        var token = generateToken(authUser);
//        var ioStreamToken = generateIoStreamToken(authUser);
        return LoginResponse.builder()
                .token(token)
                .roles(roles)
                .username(username)
                .userId(authUser.getUserId())
//                .ioStreamToken(ioStreamToken)
                .build();
    }

//    public String generateIoStreamToken(User user) throws MalformedURLException {
//        Client client = Client.builder(
//                IO_STREAM_KEY,
//                IO_STREAM_SECRET
//        ).build();
//
//        Token token = client.frontendToken(user.getUserId());
//        return token.toString();
//    }

    private String generateToken(Account account) throws JOSEException {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet
                .Builder()
                .subject(account.getUserId())
                .issuer("Thaidq")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(EXPIRATION_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(account))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        jwsObject.sign(new MACSigner(KEY));
        return jwsObject.serialize();
    }

    private String buildScope(Account account) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!account.getRoles().isEmpty()) {
            account.getRoles().forEach(role ->
                    {
                        stringJoiner.add(role.getName());
                        if (!role.getPermissions().isEmpty())
                            role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
                    }
            );
        }
        return stringJoiner.toString();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiryTime = (isRefresh) ? new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(Long.parseLong(REFRESHABLE_DURATION), ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);
        if (!(verified && expiryTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (isTokenInBlacklist(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return signedJWT;
    }

    public IntrospectResponse introspect(String token) {
        boolean isValid = true;
        try {
            verifyToken(token, false);
        } catch (Exception e) {
            isValid = false;
        }
        return IntrospectResponse.builder().valid(isValid).build();
    }

    private boolean isTokenInBlacklist(String jwtId) {
        return redisTemplate.opsForValue().get("bl-" + jwtId) != null;
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException, MalformedURLException {
        var signedJWT = verifyToken(request.getToken(), true);
        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        addTokenToBlacklist(jit, expiryTime);

        var userId = signedJWT.getJWTClaimsSet().getSubject();
        var user = accountRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return RefreshTokenResponse.builder()
//                .ioStreamToken(generateIoStreamToken(user))
                .token(generateToken(user))
                .build();
    }

    @Transactional
    public RegisterResponse registerOne(RegisterUser request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        String userId = generateUserId(request.getRole().toString(), request.getEnrollmentYear());
        String password = generateUniqueSuffix(8);
        String username = generateUsername(request.getFullName(), userId);
        log.info("userId {}", userId);
        log.info("username {}", username);
        log.info("pass {}", password);

        Account account = createAccount(userId, request.getEmail(), username, password);

        if (request instanceof RegisterStudentRequest studentRequest) {
            processUserRegistration(
                    account,
                    "STUDENT",
                    () -> saveStudentAccount(account, studentRequest)
            );
        } else if (request instanceof RegisterTeacherRequest teacherRequest) {
            processUserRegistration(
                    account,
                    "TEACHER",
                    () -> saveTeacherAccount(account, teacherRequest)
            );
        } else if (request instanceof RegisterParentRequest parentRequest) {
            processUserRegistration(
                    account,
                    "PARENT",
                    () -> saveParentAccount(account, parentRequest)
            );
        }

        sendAccountInfo(request.getEmail(), request.getFullName(), username, password);

        sendVerificationEmail(request.getEmail(), request.getFullName());

        return RegisterResponse.builder()
                .success(true)
                .failedResults(null)
                .build();
    }

    @Transactional
    public RegisterResponse registerPrincipal(RegisterUser request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        String userId = generateUserId(request.getRole().toString(), request.getEnrollmentYear());
        String password = generateUniqueSuffix(8);
        String username = generateUsername(request.getFullName(), userId);

        Account account = createAccount(userId, request.getEmail(), username, password);

        processUserRegistration(
                account,
                "PRINCIPAL",
                () -> saveTeacherAccount(account, (RegisterTeacherRequest) request)
        );

        sendAccountInfo(request.getEmail(), request.getFullName(), username, password);

        sendVerificationEmail(request.getEmail(), request.getFullName());

        return RegisterResponse.builder()
                .success(true)
                .failedResults(null)
                .build();
    }

    private Account createAccount(String userId, String email, String username, String password) {
        return Account.builder()
                .userId(userId)
                .email(email)
                .username(username)
                .password(passwordEncoder.encode(password))
                .status(Account.Status.UNVERIFIED)
                .build();
    }

    public String generateUsername(String fullName, String userId) {
        String normalizedFullName = removeDiacritics(fullName.toLowerCase().trim());
        userId = userId.toLowerCase().trim();

        String[] nameParts = normalizedFullName.split("\\s+");
        if (nameParts.length == 0) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }

        StringBuilder username = new StringBuilder();

        username.append(nameParts[nameParts.length - 1]);

        for (int i = 0; i < nameParts.length - 1; i++) {
            username.append(nameParts[i].charAt(0));
        }

        return username.append(userId).toString();
    }

    private String generateUniqueSuffix(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder suffix = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            suffix.append(characters.charAt(index));
        }

        return suffix.toString();
    }

    private String removeDiacritics(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("đ", "d")
                .replaceAll("[^a-z\\s]", "")
                .replaceAll("\\s+", " ");
    }

    private void processUserRegistration(Account account, String roleId, Runnable specificSaveLogic) {
        Set<Role> roles = Set.of(roleRepository.findById(roleId).orElseThrow());
        account.setRoles(roles);

        accountRepository.save(account);

        specificSaveLogic.run();
    }

    @Transactional
    public void saveStudentAccount(Account account, RegisterStudentRequest studentRequest) {
        Student student = studentMapper.toStudent(studentRequest);
        student.setUserId(account.getUserId());
        student.setStatus(Student.Status.STUDYING);
        studentRepository.save(student);
    }

    @Transactional
    public void saveTeacherAccount(Account account, RegisterTeacherRequest teacherRequest) {
        Teacher teacher = teacherMapper.toTeacher(teacherRequest);
        teacher.setUserId(account.getUserId());
        teacher.setDegree(teacherRequest.getDegree());
        teacher.setStatus(Teacher.Status.WORKING);
        teacherRepository.save(teacher);
    }

    @Transactional
    public void saveParentAccount(Account account, RegisterParentRequest parentRequest) {
        Parent parent = parentMapper.toParent(parentRequest);
        parent.setUserId(account.getUserId());
        parentRepository.save(parent);
        Student student = studentRepository.findByEmail(parentRequest.getStudentEmail())
                .orElseThrow(
                        () -> new AppException(ErrorCode.USER_NOT_EXISTED)
                );
        ParentStudent parentStudent = ParentStudent.builder()
                .student(student)
                .parent(parent)
                .parentType(parentRequest.getParentType())
                .build();
        parentStudentRepository.save(parentStudent);
    }

    public RegisterResponse registerBatch(MultipartFile file, String type) {
        Set<Integer> failedRows = Collections.synchronizedSet(new HashSet<>());
        Set<String> emailsInFile = Collections.synchronizedSet(new HashSet<>());
        Map<Integer, RegisterUser> userRequests = new ConcurrentHashMap<>();

        try (InputStream inputStream = file.getInputStream(); XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            XSSFSheet sheet = workbook.getSheetAt(0);

            Map<Integer, MultipartFile> imageUploads = extractImages(sheet);
            Map<Integer, PresignedUrlResponse> presignedUrls = batchGetPresignedUrls(imageUploads);

            List<CompletableFuture<Void>> uploadFutures = new ArrayList<>();

            IntStream.range(1, sheet.getLastRowNum() + 1).parallel().forEach(i -> {
                XSSFRow row = sheet.getRow(i);
                try {
                    RegisterUser request = parseRowToRegisterUser(row, type, sheet);
                    emailsInFile.add(request.getEmail());
                    userRequests.put(i, request);
                } catch (Exception ignored) {
                }
            });

            List<String> existingEmails = accountRepository.findExistingEmails(emailsInFile);

            userRequests.entrySet().parallelStream().forEach(entry -> {
                int rowIndex = entry.getKey();
                RegisterUser request = entry.getValue();

                try {
                    if (existingEmails.contains(request.getEmail())) {
                        failedRows.add(rowIndex);
                        return;
                    }

                    if (imageUploads.containsKey(rowIndex)) {
                        PresignedUrlResponse presignedResponse = presignedUrls.get(rowIndex);
                        MultipartFile imageFile = imageUploads.get(rowIndex);

                        request.setImage(presignedResponse.getImageUrl());

                        CompletableFuture<Void> uploadTask = CompletableFuture.runAsync(
                                () -> {
                                    try {
                                        uploadFile(presignedResponse.getPresignedUrl(), imageFile);
                                    } catch (IOException | URISyntaxException e) {
                                        log.error(e.getMessage());
                                        failedRows.add(rowIndex);
                                    }
                                }, taskExecutor);
                        uploadFutures.add(uploadTask);
                    }

                    CompletableFuture<Void> registerTask = CompletableFuture.runAsync(
                            () -> bridgeService.callRegisterUser(request), taskExecutor);
                    uploadFutures.add(registerTask);

                } catch (Exception e) {
                    failedRows.add(rowIndex);
                }
            });

            CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0])).join();

        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_ERROR);
        }

        return RegisterResponse.builder()
                .success(failedRows.isEmpty())
                .failedResults(failedRows.stream().toList())
                .build();
    }

    private void uploadFile(String presignedUrl, MultipartFile imageFile) throws IOException, URISyntaxException {
        URI uri = new URI(presignedUrl);
        try {
            restClient.put()
                    .uri(uri)
                    .header(HttpHeaders.CONTENT_TYPE, imageFile.getContentType())
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(imageFile.getSize()))
                    .body(imageFile.getBytes())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new IOException("S3 Upload Failed: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error when uploading to S3: {}", e.getMessage());
            throw new IOException("Unexpected S3 Upload Error: " + e.getMessage());
        }

    }

    private Map<Integer, MultipartFile> extractImages(XSSFSheet sheet) {
        Map<Integer, MultipartFile> imageMap = new HashMap<>();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        List<XSSFPicture> pictures = drawing.getShapes().stream()
                .filter(shape -> shape instanceof XSSFPicture)
                .map(shape -> (XSSFPicture) shape)
                .toList();

        for (XSSFPicture picture : pictures) {
            XSSFClientAnchor anchor = picture.getClientAnchor();
            int rowNum = anchor.getRow1();

            XSSFPictureData pictureData = picture.getPictureData();
            String ext = pictureData.suggestFileExtension();
            byte[] data = pictureData.getData();

            String fileName = "-avatar" + "." + ext;
            String contentType = "image/" + ext;

            MultipartFile multipartFile = new CustomMultipartFile(fileName, data, contentType);
            imageMap.put(rowNum, multipartFile);
        }
        return imageMap;
    }


    private Map<Integer, PresignedUrlResponse> batchGetPresignedUrls(Map<Integer, MultipartFile> files) {
        List<PresignedUrlRequest> fileRequests = files.entrySet().stream()
                .map(entry -> new PresignedUrlRequest(entry.getKey(), entry.getValue().getOriginalFilename(), entry.getValue().getContentType()))
                .collect(Collectors.toList());

        List<PresignedUrlResponse> responses = resourceServiceClient.getPresignedUrls(fileRequests).getResult();

        return responses.stream()
                .collect(Collectors.toMap(PresignedUrlResponse::getRowIndex, response -> response));
    }


    private RegisterUser parseRowToRegisterUser(Row row, String type, XSSFSheet sheet) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fullName = getCellValue(row, 1);
        String email = getCellValue(row, 2);
        String phoneNumber = getCellValue(row, 3);
        String address = getCellValue(row, 4);
        LocalDate dob = null;
        try {
            dob = LocalDate.parse(getCellValue(row, 5), dateFormatter);
        } catch (DateTimeParseException e) {
            log.error("Invalid date format in row {}: {}", row.getRowNum() + 1, getCellValue(row, 5));
            throw new AppException(ErrorCode.INVALID_DATE_FORMAT);
        }
        User.Gender gender = User.Gender.OTHER;
        if (getCellValue(row, 6).equalsIgnoreCase("male")) {
            gender = User.Gender.MALE;
        } else if (getCellValue(row, 6).equalsIgnoreCase("female")) {
            gender = User.Gender.FEMALE;
        }
        String enrollmentYear = getCellValue(row, 7);

        return switch (type.toUpperCase()) {
            case "STUDENT" -> RegisterStudentRequest.builder()
                    .fullName(fullName)
                    .email(email)
                    .phoneNumber(phoneNumber)
                    .address(address)
                    .gender(gender)
                    .dateOfBirth(dob)
                    .role("student")
                    .enrollmentYear((int) Double.parseDouble(enrollmentYear))
                    .build();
            case "TEACHER" -> {
                String specialization = getCellValue(row, 8);
                int yearsOfExperience = (int) Double.parseDouble(getCellValue(row, 9));
                String degreeRow= getCellValue(row, 10).toUpperCase();
                String degree = switch (degreeRow) {
                    case "BACHELOR" -> "BACHELOR";
                    case "MASTER" -> "MASTER";
                    case "DOCTORATE" -> "DOCTORATE";
                    case "ASSOCIATE PROFESSOR" -> "ASSOCIATE_PROFESSOR";
                    case "PROFESSOR" -> "PROFESSOR";
                    default -> throw new AppException(ErrorCode.INVALID_DEGREE);
                };
                yield RegisterTeacherRequest.builder()
                        .fullName(fullName)
                        .email(email)
                        .phoneNumber(phoneNumber)
                        .address(address)
                        .gender(gender)
                        .dateOfBirth(dob)
                        .role("teacher")
                        .specialization(specialization)
                        .enrollmentYear((int) Double.parseDouble(enrollmentYear))
                        .yearsOfExperience(yearsOfExperience)
                        .degree(Teacher.Degree.valueOf(degree))
                        .build();
            }
            case "PARENT" -> {
                String studentEmail = getCellValue(row, 8);
                ParentStudent.ParentType parentType = ParentStudent.ParentType.valueOf(getCellValue(row, 9));
                yield RegisterParentRequest.builder()
                        .fullName(fullName)
                        .email(email)
                        .phoneNumber(phoneNumber)
                        .address(address)
                        .gender(gender)
                        .dateOfBirth(dob)
                        .role("parent")
                        .parentType(parentType)
                        .isNotificationOn(false)
                        .studentEmail(studentEmail)
                        .enrollmentYear((int) Double.parseDouble(enrollmentYear))
                        .build();
            }
            default -> throw new IllegalArgumentException("Invalid user type: " + type);
        };
    }


    private String getCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return cell != null ? cell.toString().trim() : "";
    }

    private void sendVerificationEmail(String email, String fullName) {
        redisTemplate.delete("verification-" + email);
        String token = UUID.randomUUID().toString();
        // Have 5 minutes to verify account
        redisTemplate.opsForValue().set("verification-" + email, token, 5, TimeUnit.MINUTES);

        String uriString = apiUrl + "/user/identity/verify?email=" + email + "&token=" + token;

        kafkaTemplate.send(
                "verification",
                VerifyAccount.builder()
                        .email(email)
                        .fullName(fullName)
                        .url(uriString)
                        .build()
        );
        log.info("Send verification email");
    }

    private void sendAccountInfo(String email, String fullName, String username, String password) {
        kafkaTemplate.send(
                "sendAccountInfo",
                AccountInfo.builder()
                        .email(email)
                        .fullName(fullName)
                        .username(username)
                        .password(password)
                        .build()
        );
    }

    public String verifyAccount(String email, String token) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.VerificationLinkCorrupted));
        switch (account.getStatus()) {
            case BLOCKED -> throw new AppException(ErrorCode.AccountIsBlocked);
            case ACTIVATED -> throw new AppException(ErrorCode.AccountAlreadyVerified);
        }

        String storedToken = redisTemplate.opsForValue().get("verification-" + email);
        if (storedToken == null) {
            sendVerificationEmail(email, "User");
            throw new AppException(ErrorCode.VerificationLinkExpired);
        }

        if (!token.equals(storedToken)) {
            throw new AppException(ErrorCode.VerificationLinkCorrupted);
        }

        account.setStatus(Account.Status.ACTIVATED);
        accountRepository.save(account);
        redisTemplate.delete("verification-" + email);
        return VERIFY_EMAIL_REDIRECT;
    }

    private void addTokenToBlacklist(String jwtId, Date expiryTime) {
        redisTemplate.opsForValue().set("bl-" + jwtId, jwtId);
        redisTemplate.expireAt("bl-" + jwtId, expiryTime);
    }

    public void logout(String token) throws Exception {
        var signedJWT = verifyToken(token, true);
        String jId = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        addTokenToBlacklist(jId, expiryTime);
    }

    public LoginResponse outboundAuthenticate(String code) throws JOSEException {
        code = java.net.URLDecoder.decode(code, StandardCharsets.UTF_8);
        var response = outboundIdentityClient.exchangeToken(ExchangeTokenRequest.builder()
                .code(code)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri(REDIRECT_URI)
                .grantType(GRANT_TYPE)
                .build());
        log.info("TOKEN RESPONSE {}", response);

        var userInfo = outboundUserClient.getUserInfo("json", response.getAccessToken());

        log.info("User Info {}", userInfo);

        var user = accountRepository.findByEmail(userInfo.getEmail()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)
        );
        var token = generateToken(user);
//        var ioStreamToken = generateIoStreamToken(user);
        return LoginResponse.builder()
                .userId(user.getUserId())
                .token(token)
                .username(user.getUsername())
//                .ioStreamToken(ioStreamToken)
                .build();
    }

    public SendOTPResponse sendOTPForForgetPassword(String email) {
        if (!accountRepository.existsByEmail(email)) {
            return SendOTPResponse.builder().isSent(false).build();
        }
        User user = userRepository.findByEmail(email);
        String OTP = generate();
        redisTemplate.opsForValue().set("otp-" + email, OTP, 3, TimeUnit.MINUTES);
        SendOTP sendOtp = SendOTP.builder().email(email).otp(OTP).name(user.getFullName()).build();
        kafkaTemplate.send("sendOTP", sendOtp);
        return SendOTPResponse.builder().isSent(true).build();
    }

    public CheckOTPResponse checkOTP(String otp, String email) {
        if (otp.equals(redisTemplate.opsForValue().get("otp-" + email))) {
            redisTemplate.delete("otp-" + email);
            String newPassword = generateUniqueSuffix(8);
            var account = accountRepository.findByEmail(email).orElseThrow();
            account.setPassword(passwordEncoder.encode(newPassword));
            accountRepository.save(account);
            User user = userRepository.findByEmail(email);
            SendPassword sendPassword = SendPassword.builder().email(email).username(account.getUsername()).password(newPassword).name(user.getFullName()).build();
            kafkaTemplate.send("sendNewPassword", sendPassword);
            return CheckOTPResponse.builder().isValid(true).build();
        } else {
            return CheckOTPResponse.builder().isValid(false).build();
        }
    }

    private String generate() {
        int OTP = new Random().nextInt(900000) + 100000;
        return String.valueOf(OTP);
    }

    @Override
    public Map<String, Boolean> checkUserRoles(List<String> userIds, String roleName) {
        // Bước 1: Kiểm tra null hoặc rỗng, nếu có thì throw exception
        if (userIds == null || userIds.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_KEY); // 1001: "Invalid key"
        }
        // Bước 2: Loại bỏ các userId trùng lặp
        Set<String> uniqueUserIds = new HashSet<>(userIds);
        Map<String, Boolean> userRolesMap = new HashMap<>();

        // Bước 3: Kiểm tra từng user ID hợp lệ hoặc không hợp lệ
        for (String userId : uniqueUserIds) {
            // Kiểm tra user có tồn tại không
            if (!userRepository.existsById(userId)) {
                userRolesMap.put(userId, false); // Nếu user không tồn tại → false
            } else {
                // Nếu user hợp lệ, kiểm tra role
                boolean hasRole = accountRepository.findById(userId)
                        .map(account -> account.getRoles()
                                .stream()
                                .map(Role::getName)
                                .anyMatch(role -> role.equalsIgnoreCase(roleName))) // So sánh không phân biệt hoa thường
                        .orElse(false);

                userRolesMap.put(userId, hasRole);
            }
        }
        return userRolesMap;
    }

    public ChangePasswordResponse changePassword(String userId, ChangePasswordRequest request) {
        Account account = accountRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (!passwordEncoder.matches(request.getOldPassword(), account.getPassword())) {
            return ChangePasswordResponse.builder().success(false).build();
        }
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
        return ChangePasswordResponse.builder().success(true).build();
    }

    @Override
    public List<Object> findByEmails(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            throw new AppException(ErrorCode.EMAIL_INVALID);
        }

        List<Object> results = emails.stream()
                .filter(email -> email != null && !email.trim().isEmpty())
                .map(email -> accountRepository.findByEmail(email)
                        .map(account -> {
                            Map<String, String> result = new HashMap<>();
                            result.put("userId", account.getUserId());
                            return (Object) result;
                        })
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();

        if (results.isEmpty()) {
            throw new AppException(ErrorCode.EMAIL_INVALID);
        }

        return results;
    }



}
