using AutoMapper;
using DocumentFormat.OpenXml.Office2016.Excel;
using DocumentFormat.OpenXml.Spreadsheet;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using subject_service.Common.Exporting.Interfaces;
using subject_service.Common.Exporting.Models;
using subject_service.Models;
using subject_service.Repositories.Interfaces;
using subject_service.Services.Interfaces;
using System.Net.Http;

namespace subject_service.Common.Exporting.Implementations
{
    public class MarkExporter : IMarkExporter
    {
        private readonly IExcelExporter _excelExporter;
        //private readonly IMarkRepository _markRepository;
        //private readonly IComponentPointRepository _componentPointRepository;
        //private readonly IMarkTypeRepository _markTypeRepository;
        //private readonly IMarkSubjectRepository _markSubjectRepository;
        //private readonly ISubjectRepository _subjectRepository;
        //private readonly ISubjectClassRepository _subjectClassRepository;
        //private readonly IUserClientService _userClientService;
        private readonly ISchoolClientService _schoolClientService;
        private readonly IViewStudentSubjectScoreRepository _viewStudentSubjectScoreRepository;

        public MarkExporter(IExcelExporter excelExporter,
            //IMarkRepository markRepository, IComponentPointRepository componentPointRepository,
            //IMarkTypeRepository markTypeRepository, IMarkSubjectRepository markSubjectRepository, IUserClientService userClientService,
            //ISubjectRepository subjectRepository, ISubjectClassRepository subjectClassRepository,
            ISchoolClientService schoolClientService,
            IViewStudentSubjectScoreRepository viewStudentSubjectScoreRepository
            )
        {
            _excelExporter = excelExporter;
            //_markRepository = markRepository;
            //_componentPointRepository = componentPointRepository;
            //_markTypeRepository = markTypeRepository;
            //_markSubjectRepository = markSubjectRepository;
            //_userClientService = userClientService;
            //_subjectClassRepository = subjectClassRepository;
            //_subjectRepository = subjectRepository;
            _schoolClientService = schoolClientService;
            _viewStudentSubjectScoreRepository = viewStudentSubjectScoreRepository;
        }

        public async Task<(ExportResult, int)> ExportMarkSummaryAnnualAsync(long classId, long schoolYearId)
        {
            throw new NotImplementedException();
        }

        public async Task<(ExportResult, int)> ExportMarkSummaryBySemesterAsync(long classId, long semesterId, string studentId, int option)
        {
            var sheetData = new ExcelSheetData();
            int code = 1000;
            if (option == 1)
            {
                // 1. Build dữ liệu cho sheet
                var sheetDataTeacher = await BuildScoreSumarySheetDataForTeacher(classId, semesterId);
                sheetData = sheetDataTeacher.Item1;
                code = sheetDataTeacher.Item2;
            }
            else if(option == 2) 
            {
                var sheetDataStudent = await BuildScoreSumarySheetDataForStudent(classId, semesterId, studentId);
                sheetData = sheetDataStudent.Item1;
                code = sheetDataStudent.Item2;
            }

            if (code != 1000)
            {
                return (new(), code);
            }
            // 2. Gọi export
            var exportResult = await _excelExporter.ExportSheetAsync(
                    sheetData.SheetName,
                    sheetData.MainTitle,
                    sheetData.GroupHeaders,
                    sheetData.SubHeaders,
                    sheetData.Rows,
                    //sheetData.fileNameBase
                    //fileNameBase: $"PhieuDiem_Lop_{classId}_HK{semesterId}"
                    fileNameBase: sheetData.FileNameBase
            );


            return (exportResult, code);
        }

        public async Task<(ExportResult, int)> ExportSubjectScoreBySemesterAsync(
    long subjectId,
    long classId,
    long semesterId,
    string studentId,
    int option
    )
        {
            var sheetData = new ExcelSheetData();
            int code = 1000;
            if (option == 1)
            {
                // 1. Build dữ liệu cho sheet
                var sheetDataTeacher = await BuildSubjectScoreSheetDataForTeacher(classId, subjectId, semesterId);
                sheetData = sheetDataTeacher.Item1;
                code = sheetDataTeacher.Item2;
            }
            else if(option == 2)
            {
                var sheetDataTeacher = await BuildSubjectScoreSheetDataForStudent(classId, subjectId, semesterId, studentId);
                sheetData = sheetDataTeacher.Item1;
                code = sheetDataTeacher.Item2;
            }
            
            if (code != 1000)
            {
                return (new(), code);
            }

            // 2. Gọi export
            var exportResult = await _excelExporter.ExportSheetAsync(
            sheetData.SheetName,
            sheetData.MainTitle,
            sheetData.GroupHeaders,
            sheetData.SubHeaders,
            sheetData.Rows,
            //sheetData.fileNameBase,
                fileNameBase: sheetData.FileNameBase
                //fileNameBase: $"PhieuDiem_Mon_{subjectId}_Lop_{classId}_HK{semesterId}"
            );
            return (exportResult, code);
        }

        public async Task<(ExcelSheetData, int)> BuildScoreSumarySheetDataForTeacher(
        long classId,
        long semesterId)
        {
            try
            {
                // 1. Lấy dữ liệu điểm chi tiết từ view hoặc truy vấn gộp các bảng
                var data = await _viewStudentSubjectScoreRepository.GetByConditionAsync(s => s.ClassId == classId &&
                                s.SemesterId == semesterId
                                );

                var (students, codeStudent) = await _schoolClientService.GetListStudentByClassId(classId);

                string className = students.First().ClassName;

                string subjectName = data.Item1.First().SubjectName;

                if (data.Item2 != 1000)
                    return (new(), data.Item2);

                if (codeStudent != 1000 || students.IsNullOrEmpty() || className.IsNullOrEmpty())
                    return (new(), codeStudent);


                var groupedStudentName = students.GroupBy(x => x.UserId)
                    .ToDictionary(
                    g => g.Key,
                    g => g.Select(x => x.FullName).Distinct().ToList()
                    );

                var groupedIsEvaluateWithScore = data.Item1
                    .Where(s => s.IsEvaluateWithScore).GroupBy(x => x.SubjectId)
                    .ToDictionary(
                        g => g.Key,
                        g => g.Select(x => x.SubjectName).Distinct().ToList()
                    );

                var groupedIsNotEvaluateWithScore = data.Item1
                    .Where(s => !s.IsEvaluateWithScore).GroupBy(x => x.SubjectId)
                    .ToDictionary(
                        g => g.Key,
                        g => g.Select(x => x.SubjectName).Distinct().ToList()
                    );


                // 3. Chuẩn bị GroupHeaders & SubHeaders
                var groupHeaders = new List<(string Title, int Span)>
    {
        ("No.", 1),
        ("Full Name", 1),
        ("Subjects assessed by comments", groupedIsEvaluateWithScore.Count),
        ("Subjects assessed by comments combined with assessment by scores", groupedIsNotEvaluateWithScore.Count),
        ("Academic Performance", 1),
    };

                var subHeaders = new List<string> { "", "" };

                foreach (var type in groupedIsEvaluateWithScore)
                {
                    foreach (var col in type.Value)
                    {
                        subHeaders.Add(col);
                    }
                }

                foreach (var type in groupedIsNotEvaluateWithScore)
                {
                    foreach (var col in type.Value)
                    {
                        subHeaders.Add(col);
                    }
                }

                subHeaders.Add("");

                // 4. Ghi dữ liệu cho từng học sinh
                var rows = new List<List<object>>();
                int stt = 1;

                foreach (var student in data.Item1.DistinctBy(s => s.StudentId).DistinctBy(s => s.MarkSubjectId).GroupBy(x => x.StudentId).OrderBy(g => g.Key))
                {
                    var row = new List<object>
        {
            stt++,
        };
                    foreach (var type in groupedStudentName)
                    {
                        foreach (var col in type.Value)
                        {
                            if (type.Key == student.Key)
                            {
                                row.Add(col);
                                break;
                            }
                        }
                    }

                    if (row.Count == 1)
                    {
                        row.Add(student.Key);
                    }

                    // Ghi điểm cho từng môn
                    foreach (var type in groupedIsEvaluateWithScore)
                    {
                        foreach (var col in type.Value)
                        {
                            var point = student.FirstOrDefault(p => p.SubjectName == col);

                            row.Add(point?.QualitativeScore ?? "");
                        }
                    }

                    foreach (var type in groupedIsNotEvaluateWithScore)
                    {
                        foreach (var col in type.Value)
                        {
                            var point = student.FirstOrDefault(p => p.SubjectName == col);

                            row.Add(point?.Score ?? 0);
                        }
                    }

                    // Kết quả học tập
                    row.Add(student?.FirstOrDefault().AcademicPerformance ?? "");

                    // Ghi chú
                    //row.Add(student.FirstOrDefault()?.Note ?? "");

                    rows.Add(row);
                }

                // 5. Trả về dữ liệu hoàn chỉnh
                return (new ExcelSheetData
                {
                    //SheetName = $"HK{semesterId}",
                    SheetName = $"{className}",
                    MainTitle = $"SUMMARY OF SEMESTER {semesterId}",
                    //MainTitle = $"PHIẾU ĐIỂM MÔN - HỌC KÌ {semesterId}",
                    GroupHeaders = groupHeaders,
                    SubHeaders = subHeaders,
                    Rows = rows,
                    FileNameBase = $"MarkSummary_Subject_{subjectName}_Class_{className}_{semesterId}"
                }, 1000);
            }
            catch (Exception ex)
            {
                return (new(), 1003);
            }
        }
        
        public async Task<(ExcelSheetData, int)> BuildScoreSumarySheetDataForStudent(
        long classId,
        long semesterId,
        string studentId)
        {
            try
            {
                // 1. Lấy dữ liệu điểm chi tiết từ view hoặc truy vấn gộp các bảng
                var data = await _viewStudentSubjectScoreRepository.GetByConditionAsync(s => s.ClassId == classId &&
                                s.SemesterId == semesterId && s.StudentId == studentId
                                );

                var (students, codeStudent) = await _schoolClientService.GetListStudentByClassId(classId);

                string className = students.First().ClassName;

                string subjectName = data.Item1.First().SubjectName;

                string studentName = students.First().FullName.Trim();

                if (data.Item2 != 1000)
                    return (new(), data.Item2);

                if (codeStudent != 1000 || students.IsNullOrEmpty() || className.IsNullOrEmpty() || studentName.IsNullOrEmpty())
                    return (new(), codeStudent);

                var groupedStudentName = students.GroupBy(x => x.UserId)
                    .ToDictionary(
                    g => g.Key,
                    g => g.Select(x => x.FullName).Distinct().ToList()
                    );

                var groupedIsEvaluateWithScore = data.Item1
                    .Where(s => s.IsEvaluateWithScore).GroupBy(x => x.SubjectId)
                    .ToDictionary(
                        g => g.Key,
                        g => g.Select(x => x.SubjectName).Distinct().ToList()
                    );

                var groupedIsNotEvaluateWithScore = data.Item1
                    .Where(s => !s.IsEvaluateWithScore).GroupBy(x => x.SubjectId)
                    .ToDictionary(
                        g => g.Key,
                        g => g.Select(x => x.SubjectName).Distinct().ToList()
                    );


                // 3. Chuẩn bị GroupHeaders & SubHeaders
                var groupHeaders = new List<(string Title, int Span)>
                {
        ("No.", 1),
        ("Full Name", 1),
        ("Subjects assessed by comments", groupedIsEvaluateWithScore.Count),
        ("Subjects assessed by comments combined with assessment by scores", groupedIsNotEvaluateWithScore.Count),
        ("Academic Performance", 1),
                };

                var subHeaders = new List<string> { "", "" };

                foreach (var type in groupedIsEvaluateWithScore)
                {
                    foreach (var col in type.Value)
                    {
                        subHeaders.Add(col);
                    }
                }

                foreach (var type in groupedIsNotEvaluateWithScore)
                {
                    foreach (var col in type.Value)
                    {
                        subHeaders.Add(col);
                    }
                }

                subHeaders.Add("");

                // 4. Ghi dữ liệu cho từng học sinh
                var rows = new List<List<object>>();
                int stt = 1;

                foreach (var student in data.Item1.DistinctBy(s => s.MarkSubjectId).GroupBy(x => x.StudentId).OrderBy(g => g.Key))
                {
                    var row = new List<object>
        {
            stt++,
        };
                    foreach (var type in groupedStudentName)
                    {
                        foreach (var col in type.Value)
                        {
                            if (type.Key == student.Key)
                            {
                                row.Add(col);
                                break;
                            }
                        }
                    }

                    if (row.Count == 1)
                    {
                        row.Add(student.Key);
                    }

                    // Ghi điểm cho từng môn
                    foreach (var type in groupedIsEvaluateWithScore)
                    {
                        foreach (var col in type.Value)
                        {
                            var point = student.FirstOrDefault(p => p.SubjectName == col);

                            row.Add(point?.QualitativeScore ?? "");
                        }
                    }

                    foreach (var type in groupedIsNotEvaluateWithScore)
                    {
                        foreach (var col in type.Value)
                        {
                            var point = student.FirstOrDefault(p => p.SubjectName == col);

                            row.Add(point?.Score ?? 0);
                        }
                    }

                    // Kết quả học tập
                    row.Add(student?.FirstOrDefault().AcademicPerformance ?? "");

                    // Ghi chú
                    //row.Add(student.FirstOrDefault()?.Note ?? "");

                    rows.Add(row);
                }

                // 5. Trả về dữ liệu hoàn chỉnh
                return (new ExcelSheetData
                {
                    //SheetName = $"HK{semesterId}",
                    SheetName = $"{className}",
                    MainTitle = $"SUMMARY OF SEMESTER {semesterId}",
                    //MainTitle = $"PHIẾU ĐIỂM MÔN - HỌC KÌ {semesterId}",
                    GroupHeaders = groupHeaders,
                    SubHeaders = subHeaders,
                    Rows = rows,
                    FileNameBase = $"MarkSummary_Student_{studentName}_Class_{className}_{semesterId}"
                }, 1000);
            }
            catch (Exception ex)
            {
                return (new(), 1003);
            }
        }

        public async Task<(ExcelSheetData, int)> BuildSubjectScoreSheetDataForTeacher(
        long classId,
        long subjectId,
        long semesterId
        )
        {
            try
            {
                // 1. Lấy dữ liệu điểm chi tiết từ view hoặc truy vấn gộp các bảng
                var data = await _viewStudentSubjectScoreRepository.GetByConditionAsync(s => s.ClassId == classId &&
                                s.SubjectId == subjectId &&
                                s.SemesterId == semesterId
                                );

                var (students, codeStudent) = await _schoolClientService.GetListStudentByClassId(classId);

                string className = students.First().ClassName;

                string subjectName = data.Item1.First().SubjectName;


                if (data.Item2 != 1000)
                    return (new(), data.Item2);
                if (codeStudent != 1000 || students.IsNullOrEmpty() || className.IsNullOrEmpty())
                    return (new(), codeStudent);

                var groupedStudentName = students.GroupBy(x => x.UserId)
                    .ToDictionary(
                    g => g.Key,
                    g => g.Select(x => x.FullName).Distinct().ToList()
                    );

                // 2. Tự động xác định các loại điểm và số cột tương ứng
                var groupedTypes = data.Item1
                    .GroupBy(x => x.ComponentType)
                    .ToDictionary(
                        g => g.Key,
                        g => g.Select(x => x.ColumnOrder).Distinct().OrderBy(x => x).ToList()
                    );

                var isEvaluateWithScore = data.Item1.First().IsEvaluateWithScore;

                // 3. Chuẩn bị GroupHeaders & SubHeaders
                var groupHeaders = new List<(string Title, int Span)>
    {
        ("No.", 1),
        ("Full Name", 1)
    };

                var subHeaders = new List<string> { "", "" };

                foreach (var type in groupedTypes)
                {
                    groupHeaders.Add((type.Key, type.Value.Count));
                    foreach (var col in type.Value)
                    {
                        subHeaders.Add("");
                    }
                }

                // Điểm TB môn học
                groupHeaders.Add(("Average Score", 1));
                subHeaders.Add("");

                // Ghi chú / nhận xét
                groupHeaders.Add(("Comments on progress, strengths, and main limitations", 1));
                subHeaders.Add("");

                // 4. Ghi dữ liệu cho từng học sinh
                var rows = new List<List<object>>();
                int stt = 1;

                foreach (var student in data.Item1.GroupBy(x => x.StudentId).OrderBy(g => g.Key))
                {
                    var row = new List<object>
        {
            stt++,
        };

                    foreach (var type in groupedStudentName)
                    {
                        foreach (var col in type.Value)
                        {
                            if (type.Key == student.Key)
                            {
                                row.Add(col);
                                break;
                            }
                        }
                    }

                    if (row.Count == 1)
                    {
                        row.Add(student.Key);
                    }

                    // Ghi điểm cho từng loại điểm + cột
                    foreach (var type in groupedTypes)
                    {
                        foreach (var col in type.Value)
                        {
                            var point = student.FirstOrDefault(p => p.ComponentType == type.Key && p.ColumnOrder == col);
                            if (isEvaluateWithScore)
                            {
                                row.Add(point?.QualitativeScore ?? "");
                            }
                            else
                            {
                                row.Add(point?.Score ?? 0);
                            }
                        }
                    }

                        // TB môn
                    if (isEvaluateWithScore)
                    {
                        row.Add(student?.FirstOrDefault().SubjectQualitativeScoreAverage ?? "");
                    }
                    else
                    {
                        row.Add(student?.FirstOrDefault().SubjectScoreAverage ?? 0);
                    }


                    // Ghi chú
                    row.Add(student.FirstOrDefault()?.Note ?? "");

                    rows.Add(row);
                }

                // 5. Trả về dữ liệu hoàn chỉnh
                return (new ExcelSheetData
                {
                    //SheetName = $"HK{semesterId}",
                    SheetName = $"{className}",
                    MainTitle = $"SEMESTER {semesterId}",
                    GroupHeaders = groupHeaders,
                    SubHeaders = subHeaders,
                    Rows = rows,
                    FileNameBase = $"Score_Subject_{subjectName}_Class_{className}_{semesterId}"
                }, 1000);

            }
            catch (Exception ex)
            {
                return (new(), 1003);
            }
        }
        
        public async Task<(ExcelSheetData, int)> BuildSubjectScoreSheetDataForStudent(
        long classId,
        long subjectId,
        long semesterId,
        string studentId
        )
        {
            try
            {
                // 1. Lấy dữ liệu điểm chi tiết từ view hoặc truy vấn gộp các bảng
                var data = await _viewStudentSubjectScoreRepository.GetByConditionAsync(s => s.ClassId == classId &&
                                s.SubjectId == subjectId &&
                                s.SemesterId == semesterId &&
                                s.StudentId == studentId
                                );

                var (students, codeStudent) = await _schoolClientService.GetListStudentByClassId(classId);

                string className = students.First().ClassName;

                string subjectName = data.Item1.First().SubjectName;

                string studentName = students.First().FullName;


                if (data.Item2 != 1000)
                    return (new(), data.Item2);
                if (codeStudent != 1000 || students.IsNullOrEmpty() || className.IsNullOrEmpty())
                    return (new(), codeStudent);

                var groupedStudentName = students.GroupBy(x => x.UserId)
                    .ToDictionary(
                    g => g.Key,
                    g => g.Select(x => x.FullName).Distinct().ToList()
                    );

                // 2. Tự động xác định các loại điểm và số cột tương ứng
                var groupedTypes = data.Item1
                    .GroupBy(x => x.ComponentType)
                    .ToDictionary(
                        g => g.Key,
                        g => g.Select(x => x.ColumnOrder).Distinct().OrderBy(x => x).ToList()
                    );

                var isEvaluateWithScore = data.Item1.First().IsEvaluateWithScore;

                // 3. Chuẩn bị GroupHeaders & SubHeaders
                var groupHeaders = new List<(string Title, int Span)>
    {
        ("No.", 1),
        ("Full Name", 1)
    };

                var subHeaders = new List<string> { "", "" };

                foreach (var type in groupedTypes)
                {
                    groupHeaders.Add((type.Key, type.Value.Count));
                    foreach (var col in type.Value)
                    {
                        subHeaders.Add("");
                    }
                }

                // Điểm TB môn học
                groupHeaders.Add(("Average Score", 1));
                subHeaders.Add("");

                // Ghi chú / nhận xét
                groupHeaders.Add(("Comments on progress, strengths, and main limitations", 1));
                subHeaders.Add("");

                // 4. Ghi dữ liệu cho từng học sinh
                var rows = new List<List<object>>();
                int stt = 1;

                foreach (var student in data.Item1.GroupBy(x => x.StudentId).OrderBy(g => g.Key))
                {
                    var row = new List<object>
        {
            stt++,
        };

                    foreach (var type in groupedStudentName)
                    {
                        foreach (var col in type.Value)
                        {
                            if (type.Key == student.Key)
                            {
                                row.Add(col);
                                break;
                            }
                        }
                    }

                    if (row.Count == 1)
                    {
                        row.Add(student.Key);
                    }

                    // Ghi điểm cho từng loại điểm + cột
                    foreach (var type in groupedTypes)
                    {
                        foreach (var col in type.Value)
                        {
                            var point = student.FirstOrDefault(p => p.ComponentType == type.Key && p.ColumnOrder == col);
                            if (isEvaluateWithScore)
                            {
                                row.Add(point?.QualitativeScore ?? "");
                            }
                            else
                            {
                                row.Add(point?.Score ?? 0);
                            }
                        }
                    }

                    if (isEvaluateWithScore)
                    {
                        // TB môn
                        row.Add(student?.FirstOrDefault().SubjectQualitativeScoreAverage ?? "");
                    }
                    else
                    {
                        row.Add(student?.FirstOrDefault().SubjectScoreAverage ?? 0);
                    }


                    // Ghi chú
                    row.Add(student.FirstOrDefault()?.Note ?? "");

                    rows.Add(row);
                }

                // 5. Trả về dữ liệu hoàn chỉnh
                return (new ExcelSheetData
                {
                    //SheetName = $"HK{semesterId}",
                    SheetName = $"{className}",
                    MainTitle = $"SEMESTER {semesterId}",
                    GroupHeaders = groupHeaders,
                    SubHeaders = subHeaders,
                    Rows = rows,
                    FileNameBase = $"Score_Subject_{subjectName}_Class_{className}_{semesterId}"
                }, 1000);

            }
            catch (Exception ex)
            {
                return (new(), 1003);
            }
        }


        public ExcelSheetData BuildSampleSubjectTeacherSheetData()
        {
            // 1. SubHeaders (không có nhóm cột con nên để trống)
            var subHeaders = new List<string>
    {
        "", "", "", "", "", "", "", "", "", ""
    };

            // 2. GroupHeaders: Tên các cột chính
            var groupHeaders = new List<(string Title, int Span)>
    {
        ("TT", 1),
        ("Họ và tên", 1),
        ("ĐĐGtx", 4),
        ("ĐĐGgk", 1),
        ("ĐĐGck", 1),
        ("ĐTBmhkI", 1),
        ("Nhận xét sự tiến bộ, ưu điểm nổi bật, hạn chế chủ yếu", 1)
    };

            // 3. Sample Rows
            var rows = new List<List<object>>
    {
        new List<object>
        {
            1, "Nguyễn Văn A",
            8.0m, 7.0m, 9.0m, 8.0m, 7.2m, 8.0m, 9.0m,
            "Có tiến bộ rõ rệt trong học kỳ. Cần phát huy tinh thần tự học."
        },
        new List<object>
        {
            2, "Trần Thị B",
            8.5m, 6.8m, 9.2m, 8.0m, 6.2m, 7.0m, 9.0m,
            "Tiếp thu bài chưa đều, cần cố gắng hơn trong kỳ tới."
        }
            };

            // 4. Trả về SheetData hoàn chỉnh
            return new ExcelSheetData
            {
                SheetName = "HK1",
                MainTitle = "HỌC KÌ I",
                GroupHeaders = groupHeaders,
                SubHeaders = subHeaders,
                Rows = rows
            };
        }


        public ExcelSheetData BuildSampleHomeroomSummarySheetData()
        {
            // 1. SubHeaders (hàng cuối cùng chứa tên cột chi tiết)
            var subHeaders = new List<string>
    {
        "", "",
        "Giáo dục thể chất", "Nghệ thuật", "HĐ trải nghiệm", "Giáo dục địa phương",
        "Ngữ văn", "Toán",
        "Ngoại ngữ 1", "GDCD", "LS-ĐL", "KHTN", "Công nghệ", "Tin học", "Dân tộc", "Ngoại ngữ 2",
        "", ""
    };

            // 2. GroupHeaders (hàng header cần merge)
            var groupHeaders = new List<(string Title, int Span)>
    {
        ("STT", 1),
        ("Họ và tên", 1),
        ("Môn học đánh giá bằng nhận xét", 4),
        ("Môn học đánh giá hỗn hợp", 2),
        ("Môn học đánh giá bằng điểm số", 8),
        ("Điểm trung bình", 1),
        ("Kết quả học tập", 1)
    };

            // 3. Sample Rows (dữ liệu từng học sinh)
            var rows = new List<List<object>>
    {
        new List<object>
        {
            1, "Nguyễn Văn A",
            "Đ", "T", "T", "T",
            8.5m, 7.5m,
            9.0m, 8.0m, 7.5m, 6.5m, 8.0m, 9.0m, 7.0m, 6.5m, 8.0m,
            "Khá"
        },
        new List<object>
        {
            2, "Trần Thị B",
            "T", "Đ", "Đ", "T",
            6.5m, 6.0m,
            7.0m, 6.5m, 6.0m, 6.5m, 7.5m, 7.0m, 6.0m, 6.0m, 9.0m,
            "Trung bình"
        }
    };

            // 4. Trả về SheetData hoàn chỉnh
            return new ExcelSheetData
            {
                SheetName = "HK1",
                MainTitle = "TỔNG HỢP HỌC KÌ I",
                GroupHeaders = groupHeaders,
                SubHeaders = subHeaders,
                Rows = rows
            };
        }
    }
}
