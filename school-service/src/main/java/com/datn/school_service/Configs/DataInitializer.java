//package com.datn.school_service.Configs;
//
//import com.datn.school_service.Exceptions.AppException;
//import com.datn.school_service.Exceptions.ErrorCode;
//import com.datn.school_service.Models.Class;
//import com.datn.school_service.Models.*;
//import com.datn.school_service.Repository.*;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Set;
//
//@Transactional
//@Component
//public class DataInitializer implements CommandLineRunner {
//
//    @Autowired
//    private AnswerRepository answerRepository;
//
//    @Autowired
//    private QuestionRepository questionRepository;
//
//    @Autowired
//    private GradeRepository gradeRepository;
//
//    @Autowired
//    private SemesterRepository semesterRepository;
//
//    @Autowired
//    private SchoolYearRepository schoolYearRepository;
//
//    @Autowired
//    private ClassRepository classRepository;
//
//    @Autowired
//    private SchoolRepository schoolRepository;
//    @Autowired
//    private RoomRepository roomRepository;
//
//    @Autowired
//    private NewsTypeRepository newsTypeRepository;
//
//    @Autowired
//    private NewsRepository newsRepository;
//
//    @Autowired
//    private RoomTypeRepository roomTypeRepository;
//    @Autowired
//    private ViolationTypeRepository violationTypeRepository;
//
//    @Override
//    public void run(String... args) throws Exception {
//
//
//        if (gradeRepository.count() == 0) {
//            gradeRepository.saveAll(List.of(
//                    Grade.builder().gradeName("10").isActive(true).build(),
//                    Grade.builder().gradeName("11").isActive(true).build(),
//                    Grade.builder().gradeName("12").isActive(true).build()
//            ));
//            System.out.println("Dữ liệu Grade đã được thêm.");
//        } else {
//            System.out.println("Dữ liệu Grade đã tồn tại. Không thêm lại.");
//        }
//        // Insert vào bảng SchoolYear
//        if (schoolYearRepository.count() == 0) {
//            schoolYearRepository.saveAll(List.of(
//                    new SchoolYear(null, "2023-2024",
//                            LocalDate.of(2023, 9, 5), LocalDate.of(2024, 5, 31),
//                            "Năm học 2023–2024", true, null, null),
//
//                    new SchoolYear(null, "2024-2025",
//                            LocalDate.of(2024, 9, 5), LocalDate.of(2025, 5, 31),
//                            "Năm học 2024–2025", true, null, null),
//
//                    new SchoolYear(null, "2025-2026",
//                            LocalDate.of(2025, 9, 5), LocalDate.of(2026, 5, 31),
//                            "Năm học 2025–2026", true, null, null),
//
//                    new SchoolYear(null, "2026-2027",
//                            LocalDate.of(2026, 9, 5), LocalDate.of(2027, 5, 31),
//                            "Năm học 2026–2027", true, null, null),
//
//                    new SchoolYear(null, "2027-2028",
//                            LocalDate.of(2027, 9, 5), LocalDate.of(2028, 5, 31),
//                            "Năm học 2027–2028", true, null, null)
//            ));
//            System.out.println("✅ Dữ liệu SchoolYear đã được thêm thành công.");
//        } else {
//            System.out.println("ℹ️ Dữ liệu SchoolYear đã tồn tại. Không thêm lại.");
//        }
//
//
//
//
//        if (semesterRepository.count() == 0) {
//            SchoolYear schoolYear2025 = schoolYearRepository.findSchoolYearBySchoolYear("2024-2025");
//            SchoolYear schoolYear2024 = schoolYearRepository.findSchoolYearBySchoolYear("2023-2024");
//
//            if (schoolYear2024 == null || schoolYear2025 == null) {
//                System.out.println("Không thể thêm dữ liệu vào Semester vì SchoolYear không tồn tại.");
//                throw new AppException(ErrorCode.ENTITY_EMPTY, "school year");
//            }
//
//            semesterRepository.saveAll(List.of(
//                    Semester.builder().semesterName("Học kỳ 1").isActive(true).schoolYear(schoolYear2024).build(),
//                    Semester.builder().semesterName("Học kỳ 2").isActive(true).schoolYear(schoolYear2024).build(),
//                    Semester.builder().semesterName("Học kỳ 1").isActive(true).schoolYear(schoolYear2025).build(),
//                    Semester.builder().semesterName("Học kỳ 2").isActive(true).schoolYear(schoolYear2025).build()
//            ));
//
//            System.out.println("Dữ liệu Semester đã được thêm.");
//        } else {
//            System.out.println("Dữ liệu Semester đã tồn tại. Không thêm lại.");
//        }
//
//
//        // Insert vào bảng School
//        if (schoolRepository.count() == 0) {
//            schoolRepository.save(new School(
//                    null,
//                    "THPT Bùi Hữu Nghĩa",
//                    "55 đường CMT 8, P An Thới, Quận Bình Thuỷ, Thành phố Cần Thơ, Việt Nam",
//                    null,
//                    null
//            ));
//            System.out.println("Dữ liệu School đã được thêm.");
//        } else {
//            System.out.println("Dữ liệu School đã tồn tại. Không thêm lại.");
//        }
//
//        // Insert vào bảng Class
//        if (classRepository.count() == 0) {
//            SchoolYear schoolYear2025 = schoolYearRepository.findSchoolYearBySchoolYear("2024-2025");
//            SchoolYear schoolYear2024 = schoolYearRepository.findSchoolYearBySchoolYear("2023-2024");
//
//            if (schoolYear2025 == null) {
//                throw new AppException(ErrorCode.ENTITY_EMPTY, " school year");
//            }
//            if(schoolYear2024 == null || schoolYear2025 == null )
//            {
//                System.out.println("Không thể thêm dữ liệu vào Class vì SchoolYear không tồn tại.");
//            }
//            else
//            {
//
//                for (long grade = 10; grade <= 12; grade++) {
//                    Grade grad = gradeRepository.findById(grade)
//                            .orElseThrow(() -> new AppException(ErrorCode.GRADE_NOT_FOUND));
//                    for (int i = 1; i <= 10; i++) {
//                        String className = grade + "A" + i;
//                        classRepository.save(new Class(
//                                null,
//                                className,
//                                null,
//                                schoolYear2025.getSchoolYearId(),
//                                null,
//                                Class.MainSession.MORNING,
//                                true,
//                                null,
//                                null,
//                                null,
//                                grad
//                        ));
//                    }
//                }
//
//
//                for (long grade = 10; grade <= 12; grade++){
//                    Grade grad = gradeRepository.findById(grade)
//                            .orElseThrow(() -> new AppException(ErrorCode.GRADE_NOT_FOUND));
//
//                    for (int i = 1; i <= 10; i++) {
//                        String className = grade + "A" + i;
//                        classRepository.save(new Class(
//                                null,
//                                className,
//                                null,
//                                schoolYear2024.getSchoolYearId(),
//                                null,
//                                Class.MainSession.MORNING,
//                                true,
//                                null,
//                                null,
//                                null,
//                                grad
//
//
//                        ));
//                    }
//                    System.out.println("Dữ liệu Class đã được thêm.");
//                }
//            }
//
//        } else {
//            System.out.println("Dữ liệu Class đã tồn tại. Không thêm lại.");
//        }
//        if (roomTypeRepository.count() == 0) {
//            roomTypeRepository.saveAll(List.of(
//                    RoomType.builder()
//                            .roomTypeName("PhongHocLyThuyet")
//                            .roomTypeDescription("Phòng học lý thuyết chung cho học sinh các lớp.")
//                            .build(),
//                    RoomType.builder()
//                            .roomTypeName("PhongMayTinh")
//                            .roomTypeDescription("Phòng máy tính phục vụ môn Tin học.")
//                            .build(),
//                    RoomType.builder()
//                            .roomTypeName("PhongThiNghiem")
//                            .roomTypeDescription("Phòng thí nghiệm cho các môn Vật lý, Hóa học, Sinh học.")
//                            .build(),
//                    RoomType.builder()
//                            .roomTypeName("PhongThuVien")
//                            .roomTypeDescription("Phòng thư viện với tài liệu, sách tham khảo.")
//                            .build(),
//                    RoomType.builder()
//                            .roomTypeName("PhongHoiTruong")
//                            .roomTypeDescription("Phòng hội trường dùng để tổ chức họp, văn nghệ, sự kiện.")
//                            .build(),
//                    RoomType.builder()
//                            .roomTypeName("PhongYTe")
//                            .roomTypeDescription("Phòng y tế chăm sóc sức khỏe học sinh.")
//                            .build(),
//                    RoomType.builder()
//                            .roomTypeName("PhongGiaoVien")
//                            .roomTypeDescription("Phòng làm việc của giáo viên.")
//                            .build(),
//                    RoomType.builder()
//                            .roomTypeName("PhongBanGiamHieu")
//                            .roomTypeDescription("Phòng làm việc của Ban giám hiệu nhà trường.")
//                            .build()
//            ));
//
//            System.out.println("Dữ liệu RoomType vừa được thêm.");
//        } else {
//            System.out.println("Dữ liệu RoomType đã được thêm vào trước đó .");
//        }
//
//
//        if (roomRepository.count() == 0) {
//            RoomType type1 = roomTypeRepository.findById(1L)
//                    .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND, "ID 1"));
//            RoomType type2 = roomTypeRepository.findById(2L)
//                    .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND, "ID 2"));
//
//            roomRepository.saveAll(List.of(
//                    Room.builder().roomName("A10").roomFloor(1L).numberOfChalkboard(1L).numberOfDevice(1L).roomType(type1).active(true).build(),
//                    Room.builder().roomName("A11").roomFloor(1L).numberOfChalkboard(1L).numberOfDevice(1L).roomType(type1).active(true).build(),
//                    Room.builder().roomName("A12").roomFloor(1L).numberOfChalkboard(20L).numberOfDevice(20L).roomType(type1).active(true).build(),
//                    Room.builder().roomName("B10").roomFloor(1L).numberOfChalkboard(20L).numberOfDevice(20L).roomType(type1).active(true).build(),
//                    Room.builder().roomName("B11").roomFloor(1L).numberOfChalkboard(20L).numberOfDevice(20L).roomType(type1).active(true).build(),
//                    Room.builder().roomName("B12").roomFloor(1L).numberOfChalkboard(20L).numberOfDevice(20L).roomType(type1).active(true).build(),
//                    Room.builder().roomName("C10").roomFloor(1L).numberOfChalkboard(20L).numberOfDevice(20L).roomType(type1).active(true).build(),
//                    Room.builder().roomName("C11").roomFloor(1L).numberOfChalkboard(20L).numberOfDevice(20L).roomType(type1).active(true).build(),
//                    Room.builder().roomName("C12").roomFloor(1L).numberOfChalkboard(20L).numberOfDevice(20L).roomType(type1).active(true).build(),
//                    Room.builder().roomName("A13").roomFloor(2L).numberOfChalkboard(20L).numberOfDevice(20L).roomType(type2).active(true).build(),
//                    Room.builder().roomName("A14").roomFloor(2L).numberOfChalkboard(20L).numberOfDevice(20L).roomType(type2).active(true).build(),
//                    Room.builder().roomName("A15").roomFloor(2L).numberOfChalkboard(20L).numberOfDevice(20L).roomType(type2).active(true).build(),
//                    Room.builder().roomName("B13").roomFloor(2L).numberOfChalkboard(20L).numberOfDevice(20L).roomType(type2).active(true).build(),
//                    Room.builder().roomName("B14").roomFloor(2L).numberOfChalkboard(20L).numberOfDevice(20L).roomType(type2).active(true).build(),
//                    Room.builder().roomName("B15").roomFloor(2L).numberOfChalkboard(20L).numberOfDevice(20L).roomType(type2).active(true).build(),
//                    Room.builder().roomName("C13").roomFloor(2L).numberOfChalkboard(20L).numberOfDevice(20L).roomType(type2).active(true).build(),
//                    Room.builder().roomName("C14").roomFloor(2L).numberOfChalkboard(20L).numberOfDevice(20L).roomType(type2).active(true).build(),
//                    Room.builder().roomName("C15").roomFloor(2L).numberOfChalkboard(20L).numberOfDevice(20L).roomType(type2).active(true).build()
//            ));
//
//            System.out.println("Dữ liệu Room đã được thêm.");
//        } else {
//            System.out.println("Dữ liệu Room đã tồn tại. Không thêm lại.");
//        }
//
//        if (newsTypeRepository.count() == 0) {
//            newsTypeRepository.saveAll(List.of(
//                    NewsType.builder()
//                            .newsTypeName("ThongBaoHocTap")
//                            .newsTypeDescription("Thông báo liên quan đến học tập, lịch thi, nghỉ học,...")
//                            .build(),
//                    NewsType.builder()
//                            .newsTypeName("TuyenSinh")
//                            .newsTypeDescription("Thông tin tuyển sinh các cấp học, xét tuyển,...")
//                            .build(),
//                    NewsType.builder()
//                            .newsTypeName("HoatDongNgoaiKhoa")
//                            .newsTypeDescription("Các hoạt động ngoại khóa, dã ngoại, cuộc thi,...")
//                            .build(),
//                    NewsType.builder()
//                            .newsTypeName("TinTucNhaTruong")
//                            .newsTypeDescription("Cập nhật tin tức về hoạt động của nhà trường")
//                            .build(),
//                    NewsType.builder()
//                            .newsTypeName("TinNoiBoGiaoVien")
//                            .newsTypeDescription("Tin tức nội bộ dành riêng cho giáo viên, nhân viên")
//                            .build()
//            ));
//
//            System.out.println("Dữ liệu NewsType vừa được thêm.");
//        } else {
//            System.out.println("Dữ liệu NewsType đã có tồn tại.");
//        }
//
//
//
//        // Insert Answer + Question (Many-to-Many)
//        if (questionRepository.count() == 0 && answerRepository.count() == 0) {
//            // Tạo các Answer mẫu
//            Answer a1 = answerRepository.save(Answer.builder().answer("A. Trễ giờ").answerPoint(1).build());
//            Answer a2 = answerRepository.save(Answer.builder().answer("B. Thỉnh thoảng đúng giờ").answerPoint(2).build());
//            Answer a3 = answerRepository.save(Answer.builder().answer("C. Đúng giờ").answerPoint(3).build());
//            Answer a4 = answerRepository.save(Answer.builder().answer("D. Luôn luôn đến sớm").answerPoint(4).build());
//
//            Answer a5 = answerRepository.save(Answer.builder().answer("A. Dạy dở").answerPoint(1).build());
//            Answer a6 = answerRepository.save(Answer.builder().answer("B. Thỉnh thoảng dạy dở").answerPoint(2).build());
//            Answer a7 = answerRepository.save(Answer.builder().answer("C. Dạy được").answerPoint(3).build());
//            Answer a8 = answerRepository.save(Answer.builder().answer("D.  Dạy Tốt").answerPoint(4).build());
//
//
//            // Tạo các Question mẫu và gán các Answer
//            Question q1 = Question.builder()
//                    .question("Giờ đến lớp của giáo viên?")
//                    .answers(Set.of(a1, a2, a3, a4))
//                    .build();
//
//            Question q2 = Question.builder()
//                    .question("Kỹ năng sư phạm của giáo viên?")
//                    .answers(Set.of(a5, a6, a7, a8))
//                    .build();
//
//            questionRepository.saveAll(List.of(q1, q2));
//
//            System.out.println("Dữ liệu Question và Answer đã được thêm.");
//        } else {
//            System.out.println("Dữ liệu Question và Answer đã tồn tại. Không thêm lại.");
//        }
//       // if(ViolationRecordRepository.)
//        if(newsRepository.count() == 0)
//        {
//            List<News> newsList = List.of(
//                    News.builder()
//
//                            .newsTitle("Exam Schedule Released")
//                            .newsContent("Final exams for all grades will start on June 1st. Check the timetable on the school portal.")
//                            .newsOwner("80847a56-4102-418c-b455-f49597bf39af")
//                            .newsTypeId(1L)
//                            .build(),
//                    News.builder()
//
//                            .newsTitle("Admission 2025 Opens")
//                            .newsContent("Admission process for the academic year 2025-2026 is open. Visit the website for details.")
//                            .newsOwner("80847a56-4102-418c-b455-f49597bf39af")
//                            .newsTypeId(2L)
//                            .build(),
//                    News.builder()
//
//                            .newsTitle("Outdoor Activity Day")
//                            .newsContent("Join the outdoor activity day this Saturday. Wear sportswear and bring water.")
//                            .newsOwner("80847a56-4102-418c-b455-f49597bf39af")
//                            .newsTypeId(3L)
//                            .build(),
//                    News.builder()
//
//                            .newsTitle("Renovation Notice")
//                            .newsContent("Block A is under renovation. Classes will be temporarily moved to Block B.")
//                            .newsOwner("80847a56-4102-418c-b455-f49597bf39af")
//                            .newsTypeId(4L)
//                            .build(),
//                    News.builder()
//
//                            .newsTitle("Staff Meeting Reminder")
//                            .newsContent("All teachers are invited to the weekly staff meeting this Friday at 3 PM.")
//                            .newsOwner("80847a56-4102-418c-b455-f49597bf39af")
//                            .newsTypeId(5L)
//                            .build()
//            );
//
//            newsRepository.saveAll(newsList);
//            System.out.println("Dữ liệu News đã được thêm.");
//        }
////        if (violationTypeRepository.count() == 0) {
////            violationTypeRepository.saveAll(List.of(
////                    ViolationType.builder()
////                            .violationTypeName("Chưa đổ rác")
////                            .violationPoint(1)
////                            .
////                            .records(null)
////                            .build(),
////                    ViolationType.builder()
////                            .violationTypeName("Không phù hiệu")
////                            .violationPoint(1)
////                            .records(null)
////                            .build(),
////                    ViolationType.builder()
////                            .violationTypeName("Không dây nịt")
////                            .violationPoint(1)
////                            .records(null)
////                            .build(),
////                    ViolationType.builder()
////                            .violationTypeName("Lớp dơ")
////                            .violationPoint(2)
////                            .records(null)
////                            .build(),
////                    ViolationType.builder()
////                            .violationTypeName("Lớp mất trật tự")
////                            .violationPoint(3)
////                            .records(null)
////                            .build()
////            ));
////            System.out.println("Dữ liệu ViolationType đã được thêm.");
////        } else {
////            System.out.println("Dữ liệu ViolationType đã tồn tại. Không thêm lại.");
////        }
//
//
//        System.out.println("Dữ liệu đã được kiểm tra và xử lý xong.");
//    }
//}
//
