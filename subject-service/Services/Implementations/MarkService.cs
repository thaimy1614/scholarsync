using AutoMapper;
using Microsoft.IdentityModel.Tokens;
using subject_service.Common.Pagination;
using subject_service.Common.Results;
using subject_service.Dtos;
using subject_service.Dtos.Mark;
using subject_service.Dtos.Student;
using subject_service.Enums;
using subject_service.Models;
using subject_service.Repositories.Interfaces;
using subject_service.Services.Interfaces;
using System.Text.Json;

namespace subject_service.Services.Implementations
{
    public class MarkService : IMarkService
    {
        private readonly IMarkRepository _markRepository;
        private readonly IComponentPointRepository _componentPointRepository;
        private readonly IMarkTypeRepository _markTypeRepository;
        private readonly IMarkSubjectRepository _markSubjectRepository;
        private readonly ISubjectRepository _subjectRepository;
        private readonly ISubjectClassRepository _subjectClassRepository;
        private readonly IUserClientService _userClientService;
        private readonly IViewStudentSubjectScoreRepository _viewStudentSubjectScoresRepository;
        private readonly IMapper _mapper;
        private readonly HttpClient _httpClient;
        private string _apiUserUrl = "";

        public MarkService(IMarkRepository markRepository, IComponentPointRepository componentPointRepository, 
            IMarkTypeRepository markTypeRepository, IMarkSubjectRepository markSubjectRepository, IUserClientService userClientService,
            ISubjectRepository subjectRepository, ISubjectClassRepository subjectClassRepository,
            IViewStudentSubjectScoreRepository viewStudentSubjectScoresRepository, IMapper mapper, HttpClient httpClient)
        {
            _markRepository = markRepository;
            _componentPointRepository = componentPointRepository;
            _markTypeRepository = markTypeRepository;
            _markSubjectRepository = markSubjectRepository;
            _userClientService = userClientService;
            _subjectClassRepository = subjectClassRepository;
            _subjectRepository = subjectRepository;
            _viewStudentSubjectScoresRepository = viewStudentSubjectScoresRepository;
            _mapper = mapper;
            _httpClient = httpClient;
        }

        public async Task<(int, List<string>)> AddMark(List<MarkCreateUpdateDto> markCreateDtos)
        {
            //_apiUserUrl = Environment.GetEnvironmentVariable("USER_SERVICE");
            /*if (!string.IsNullOrEmpty(_apiUserUrl))
            {
                try
                {
                    List<string> users = new List<string>();
                    users.AddRange(markCreateDtos.Select(s => s.student_id));
                    string queryString = string.Join("&", users.Select(id => $"userIds={id}"));
                    var userApiCheckRole = await _httpClient.GetAsync($"{_apiUserUrl}/user/checkRoleUsers/student?{queryString}");
                    var userApiResponse = await userApiCheckRole.Content.ReadAsStringAsync();
                    var apiResponse = JsonSerializer.Deserialize<ApiResponse<Dictionary<string, bool>>>(userApiResponse, new JsonSerializerOptions
                    {
                        PropertyNameCaseInsensitive = true
                    });

                    if (userApiResponse == null || !userApiCheckRole.IsSuccessStatusCode)
                    {
                        code = 1004;
                    }

                    else
                    {
                        //var marks = _mapper.Map<List<Mark>>(markCreateDtos);
                        code = 1000;
                        foreach (var item in apiResponse.Result)
                        {
                            if (!item.Value)
                            {
                                errorUser.Add(item.Key);
                                markCreateDtos.Remove(markCreateDtos.First(s => errorUser.Contains(s.student_id)));
                            }
                        }
                        foreach (var markCreateDto in markCreateDtos)
                        {
                            //var codeResult = await _markService.AddMark(item);
                            var mark = _mapper.Map<Mark>(markCreateDto);
                            var codeResult = await _markRepository.AddAsync(mark);
                            if (codeResult != 1000)
                            {
                                code = codeResult;
                                errorUser.Add(markCreateDto.student_id);
                            }
                        }
                    }
                }
                catch (Exception ex)
                {
                    code = 1004;
                }
            }
            else
            {
                code = 1004;
            }*/
            //int code;
            //return _markRepository.AddAsync(mark);


            var entities = _mapper.Map<List<Mark>>(markCreateDtos);
            List<string> errorUser = new();
            var userIds = new List<string>();
            userIds.AddRange(markCreateDtos.Select(s => s.StudentId));
            var studentIds = await _userClientService.GetAllIdsRoleStudentById(userIds);
            int code = studentIds.Item2;

            if(code == 1000)
            {
                errorUser = userIds.Except(studentIds.Item1).ToList();
                entities = entities.Where(s => !errorUser.Contains(s.StudentId)).ToList();
                foreach (var entity in entities)
                {
                    var codeResult = await _markRepository.AddAsync(entity);
                    if (codeResult != 1000)
                    {
                        code = codeResult;
                        errorUser.Add(entity.StudentId);
                    }
                }
            }

            if (errorUser.Count == userIds.Count)
            {
                code = 1002;
            }


            return (code, errorUser);
        }

        public Task<int> DeleteMark(long id)
        {
            return _markRepository.DeleteAsync(id);
        }

        public async Task<int> GenerateMarksForAllSubjectsInClass(
            long classId,
            long totalSemester,
            long schoolYearId,
            List<string> studentIds)
        {
            int code = 1000;
            try
            {
                foreach (var studentId in studentIds)
                {
                    for (int i = 1; i <= totalSemester; i++)
                    {
                        var markCreate = new Mark();
                        markCreate.StudentId = studentId;
                        markCreate.SemesterId = i;
                        markCreate.ClassId = classId;
                        markCreate.SchoolYearId = schoolYearId;
                        code = await _markRepository.AddAsync(markCreate);
                        if (code == 1000)
                        {
                            var subjectClasses = await _subjectClassRepository.GetByConditionAsync(s => s.ClassId == classId);
                            code = subjectClasses.Item2;
                            if (code == 1000)
                            {
                                var subjectIds = subjectClasses.Item1.Select(s => s.SubjectId).Distinct().ToList();
                                var subjectIsScoreables = await _subjectRepository.GetByConditionAsync(s => subjectIds.Contains(s.Id) && s.IsScoreable);
                                code = subjectIsScoreables.Item2;
                                if (code == 1000)
                                {
                                    foreach (var subject in subjectIsScoreables.Item1)
                                    {
                                        var markSubjectCreate = new MarkSubject();
                                        markSubjectCreate.SubjectId = subject.Id;
                                        markSubjectCreate.MarkId = markCreate.Id;
                                        code = await _markSubjectRepository.AddAsync(markSubjectCreate);
                                        if (code == 1000)
                                        {
                                            var markTypes = await _markTypeRepository.GetByConditionAsync(s => s.SubjectId == subject.Id && s.ClassId == classId);
                                            code = markTypes.Item2;
                                            if (code == 1000)
                                            {
                                                foreach (var markType in markTypes.Item1)
                                                {
                                                    if (code != 1000)
                                                        break;
                                                    for (int j = 1; j <= markType.TotalColumn; j++)
                                                    {
                                                        if (code != 1000)
                                                            break;
                                                        var componentPointCreate = new ComponentPoint();
                                                        componentPointCreate.ColumnOrder = j;
                                                        componentPointCreate.MarkSubjectId = markSubjectCreate.Id;
                                                        componentPointCreate.MarkTypeId = markType.Id;
                                                        componentPointCreate.IsPassFailType = subject.IsEvaluateWithScore;
                                                        code = await _componentPointRepository.AddAsync(componentPointCreate);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return code;
            }
            catch (Exception ex)
            {
                return 1001;
            }







            //var markTypes = await _subjectRepository.GetByIdAsync();
            //if (markTypes == null || !markTypes.Any()) return (0, 0);

            //var subjectIds = markTypes.Select(mt => mt.SubjectId).Distinct().ToList();
            ////int created = 0, skipped = 0;

            //foreach (var studentId in studentIds)
            //{
            //    // 2. Kiểm tra nếu đã tồn tại Mark cho học sinh này thì skip
            //    //var existingMark = await _markRepo.GetByStudentClassSemesterAsync(studentId, classId, semesterId, schoolYearId);
            //    //if (existingMark != null)
            //    //{
            //    //    skipped++;
            //    //    continue;
            //    //}

            //    // 3. Tạo Mark
            //    var newMark = new Mark
            //    {
            //        StudentId = studentId,
            //        ClassId = classId,
            //        SemesterId = semesterId,
            //        SchoolYearId = schoolYearId,
            //        IsDeleted = false
            //    };
            //    await _markRepo.AddAsync(newMark);

            //    // 4. Duyệt qua các môn
            //    foreach (var subjectId in subjectIds)
            //    {
            //        var markSubject = new MarkSubject
            //        {
            //            MarkId = newMark.Id,
            //            SubjectId = subjectId,
            //            IsDeleted = false
            //        };
            //        await _markSubjectRepo.AddAsync(markSubject);

            //        // 5. Tạo ComponentPoint từ MarkType
            //        var typesOfSubject = markTypes.Where(mt => mt.SubjectId == subjectId).ToList();
            //        foreach (var markType in typesOfSubject)
            //        {
            //            for (int i = 1; i <= markType.TotalColumn; i++)
            //            {
            //                var componentPoint = new ComponentPoint
            //                {
            //                    MarkSubjectId = markSubject.Id,
            //                    MarkTypeId = markType.Id,
            //                    ColumnOrder = i,
            //                    IsDeleted = false,
            //                    IsPassFailType = markType.IsPassFailType // hoặc false nếu chưa có
            //                };
            //                await _componentPointRepo.AddAsync(componentPoint);
            //            }
            //        }
            //    }

            //    created++;
            //}

            //return (created, skipped);
        }

        /// <summary>
        /// update logic get other api
        /// </summary>
        /// <param name="classId"></param>
        /// <param name="semesterId"></param>
        /// <param name="subjectId"></param>
        /// <returns></returns>
        public async Task<(List<StudentSubjectScoreDto>, int)> GetAllStudentScoresByClassIdSemesterIdSubjectId(long classId, long schoolYearId, long semesterId, long subjectId)
        {
            var studentSubjectScores = await _viewStudentSubjectScoresRepository.GetByConditionAsync(s => s.ClassId == classId && s.SemesterId == semesterId);
            var marks = await _markRepository.GetByConditionAsync(s => s.ClassId == classId && s.SchoolYearId == schoolYearId && s.SemesterId == semesterId);
            var studentSubjectScoreDtos = _mapper.Map<List<StudentSubjectScoreDto>>(studentSubjectScores.Item1);
            int code = studentSubjectScores.Item2;
            if(code != 1000)
            {
                return (new(), code);
            }
            _apiUserUrl = Environment.GetEnvironmentVariable("USER_SERVICE");
            if (!string.IsNullOrEmpty(_apiUserUrl))
            {
                try
                {
                    string queryString = string.Join("&", studentSubjectScores.Item1.Select(s => $"ids={s.StudentId}"));
                    var userApiUserByIds = await _httpClient.GetAsync($"{_apiUserUrl}/user/by-ids?{queryString}");
                    var userApiResponse = await userApiUserByIds.Content.ReadAsStringAsync();
                    var apiResponse = JsonSerializer.Deserialize<ApiResponse<List<StudentDto>>>(userApiResponse, new JsonSerializerOptions
                    {
                        PropertyNameCaseInsensitive = true
                    });

                    if (userApiResponse == null || !userApiUserByIds.IsSuccessStatusCode)
                    {
                        code = 1003;
                    }

                    else
                    {
                        List<StudentDto> studentNames = apiResponse.Result.ToList();
                        foreach (var studentSubjectScoreDto in studentSubjectScoreDtos)
                        {
                            foreach (var studentName in studentNames)
                            {
                                if (studentSubjectScoreDto.student_id == studentName.UserId)
                                {
                                    studentSubjectScoreDto.full_name = studentName.FullName;
                                    studentSubjectScoreDto.image = studentName.Image;
                                    break;
                                }
                            }
                            var studentScores = new List<StudentScoreDto>();
                            var componentPoints = await _componentPointRepository.GetByConditionAsync(s => s.MarkSubjectId == studentSubjectScoreDto.mark_id);
                            if (componentPoints.Item2 != 1000)
                            {
                                return (new(), componentPoints.Item2);
                            }

                            /*foreach (var componentPoint in componentPoints.Item1)
                            {
                                //var componentPointMarkType = await _componentPointMarkTypeRepository.GetByConditionAsync(s => s.component_point_id == componentPoint.id);
                                var componentPointMarkType = 1001;
                                if (componentPointMarkType != 1000)
                                {
                                    return (new(), componentPointMarkType.Item2);
                                }
                                var marktypes = await _markTypeRepository.GetByIdAsync(componentPointMarkType.Item1[0].mark_type_id);
                                if (marktypes.Item2 != 1000)
                                {
                                    return (new(), marktypes.Item2);
                                }
                                studentScores.Add(new StudentScoreDto { name_type = marktypes.Item1.name_type, score = componentPoint.score });
                            }*/
                            studentSubjectScoreDto.StudentScores = studentScores;
                        }
                    }
                }
                catch (Exception ex)
                {
                    code = 1003;
                }
            }
            else
            {
                code = 1003;
            }
            return (studentSubjectScoreDtos, code);

            /*var studentSubjectScores =  await _viewStudentSubjectScoresRepository.GetByConditionAsync(s => s.class_id == classId && s.semester_id == semesterId && s.subject_id == subjectId);
            if(studentSubjectScores.Item2 != 1000)
            {
                return (new(),  studentSubjectScores.Item2);
            }
            for(int i = 0; i < studentSubjectScores.Item1.Count; i++)
            {
                var studentScores = new List<StudentScoreDto>();
                var componentPoints = await _componentPointRepository.GetByConditionAsync(s => s.mark_id == studentSubjectScores.Item1[i].mark_id);
                if (componentPoints.Item2 != 1000)
                {
                    return (new(), componentPoints.Item2);
                }

                foreach (var componentPoint in componentPoints.Item1)
                {
                    var componentPointMarkType = await _componentPointMarkTypeRepository.GetByConditionAsync(s => s.component_point_id == componentPoint.id);
                    if (componentPointMarkType.Item2 != 1000)
                    {
                        return (new(), componentPointMarkType.Item2);
                    }
                    var marktypes = await _markTypeRepository.GetByIdAsync(componentPointMarkType.Item1[0].mark_type_id);
                    if (marktypes.Item2 != 1000)
                    {
                        return (new(), marktypes.Item2);
                    }
                    studentScores.Add(new StudentScoreDto { name_type = marktypes.Item1.name_type, score = componentPoint.score });
                }
                studentSubjectScores.Item1[i].StudentScores = studentScores;
            }
            return studentSubjectScores;*/
        }
        
        /// <summary>
        /// update logic get other api
        /// </summary>
        /// <param name="classId"></param>
        /// <param name="semesterId"></param>
        /// <returns></returns>
        public async Task<(List<StudentSubjectScoreDto>, int)> GetAllStudentScoresByClassIdSemesterId(long classId, long schoolYearId, long semesterId)
        {
            var studentSubjectScores = await _viewStudentSubjectScoresRepository.GetByConditionAsync(s => s.ClassId == classId && s.SemesterId == semesterId);
            var studentSubjectScoreDtos = _mapper.Map<List<StudentSubjectScoreDto>>(studentSubjectScores.Item1);
            int code = studentSubjectScores.Item2;
            if (code != 1000)
            {
                return (new(), code);
            }
            _apiUserUrl = Environment.GetEnvironmentVariable("USER_SERVICE");
            if (!string.IsNullOrEmpty(_apiUserUrl))
            {
                try
                {
                    string queryString = string.Join("&", studentSubjectScores.Item1.Select(s => $"ids={s.StudentId}"));
                    var userApiUserByIds = await _httpClient.GetAsync($"{_apiUserUrl}/user/by-ids?{queryString}");
                    var userApiResponse = await userApiUserByIds.Content.ReadAsStringAsync();
                    var apiResponse = JsonSerializer.Deserialize<ApiResponse<List<StudentDto>>>(userApiResponse, new JsonSerializerOptions
                    {
                        PropertyNameCaseInsensitive = true
                    });

                    if (userApiResponse == null || !userApiUserByIds.IsSuccessStatusCode)
                    {
                        code = 1003;
                    }

                    else
                    {
                        List<StudentDto> studentNames = apiResponse.Result.ToList();
                        foreach (var studentSubjectScoreDto in studentSubjectScoreDtos)
                        {
                            foreach (var studentName in studentNames)
                            {
                                if (studentSubjectScoreDto.student_id == studentName.UserId)
                                {
                                    studentSubjectScoreDto.full_name = studentName.FullName;
                                    studentSubjectScoreDto.image = studentName.Image;
                                    break;
                                }
                            }
                        }
                    }
                }
                catch (Exception ex)
                {
                    code = 1003;
                }
            }
            else
            {
                code = 1003;
            }
            return (studentSubjectScoreDtos, code);
        }
        
        public async Task<(PagedResult<MarkWithDeletedDto>, int)> GetAllMarks(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await _markRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, true);
            var markWithDeletedDtos = _mapper.Map<PagedResult<MarkWithDeletedDto>>(result.Item1);
            return (markWithDeletedDtos, result.Item2);
        }
        
        public async Task<(PagedResult<MarkDto>, int)> GetAllExistingMarks(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await _markRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, false);
            var markDtos = _mapper.Map<PagedResult<MarkDto>>(result.Item1);
            return (markDtos, result.Item2);
        }

        public async Task<(MarkDto, int)> GetMarkById(long id)
        {
            var result = await _markRepository.GetByIdAsync(id);
            var markDto = _mapper.Map<MarkDto>(result.Item1);
            return (markDto, result.Item2);
        }

        public async Task<(Dictionary<string, int>, int)> GetAcademicPerformanceSummaryByClassIdSemesterId(long classId, long? semesterId)
        {
            /*var studentSubjectScore = await _viewStudentSubjectScoresRepository.GetByConditionAsync(s => s.class_id == classId && s.semester_id == semesterId);
            var academicPerformanceSummary = new Dictionary<string, int>();
            int code = studentSubjectScore.Item2;
            if (code == 1000)
            {
                academicPerformanceSummary = studentSubjectScore.Item1
                .GroupBy(m => new { m.id, m.academic_performance })
                .Select(g => g.Key.academic_performance)
                .GroupBy(ap => ap)
                .Select(g => new
                {
                    AcademicPerformance = g.Key ?? "Unknown",
                    Count = g.Count()
                })
                .ToDictionary(x => x.AcademicPerformance, x => x.Count);
            }
            return (academicPerformanceSummary, code);*/
            var marks = await _markRepository.GetByConditionAsync(s => s.ClassId == classId && (semesterId == null || s.SemesterId == semesterId ));
            var academicPerformanceSummary = new Dictionary<string, int>();
            int code = marks.Item2;
            if (code == 1000)
            {
                academicPerformanceSummary = marks.Item1
                    .GroupBy(m => new { m.Id, m.AcademicPerformance })
                    .Select(g => g.Key.AcademicPerformance)
                    .GroupBy(ap => ap)
                    .Select(g => new
                    {
                        AcademicPerformance = g.Key ?? "Unknown",
                        Count = g.Count()
                    })
                    .ToDictionary(x => x.AcademicPerformance, x => x.Count);
            }
            return (academicPerformanceSummary, code);
        }
        
        public async Task<(Dictionary<string, int>, int)> GetAcademicPerformanceSummaryBySchoolYearIdSemesterId(long schoolYearId, long? semesterId)
        {
            var marks = await _markRepository.GetByConditionAsync(s => s.SchoolYearId == schoolYearId && (semesterId == null || s.SemesterId == semesterId));
            var academicPerformanceSummary = new Dictionary<string, int>();
            int code = marks.Item2;
            if (code == 1000)
            {
                academicPerformanceSummary = marks.Item1
                    .GroupBy(m => new { m.Id, m.AcademicPerformance })
                    .Select(g => g.Key.AcademicPerformance)
                    .GroupBy(ap => ap)
                    .Select(g => new
                    {
                        AcademicPerformance = g.Key ?? "Unknown",
                        Count = g.Count()
                    })
                    .ToDictionary(x => x.AcademicPerformance, x => x.Count);
            }
            return (academicPerformanceSummary, code);
        }
        
        public async Task<(Dictionary<string, int>, int)> GetAcademicPerformanceSummaryByClassIdSchoolYearIdSemesterId(long? classId, long? schoolYearId, long? semesterId)
        {
            var marks = await _markRepository.GetByConditionAsync(s => (classId == null || s.ClassId == classId) && (schoolYearId == null || s.SchoolYearId == schoolYearId) && (semesterId == null || s.SemesterId == semesterId));
            var academicPerformanceSummary = new Dictionary<string, int>();
            int code = marks.Item2;
            if (code == 1000)
            {
                academicPerformanceSummary = marks.Item1
                    .GroupBy(m => new { m.Id, m.AcademicPerformance })
                    .Select(g => g.Key.AcademicPerformance)
                    .GroupBy(ap => ap)
                    .Select(g => new
                    {
                        AcademicPerformance = g.Key ?? "Unknown",
                        Count = g.Count()
                    })
                    .ToDictionary(x => x.AcademicPerformance, x => x.Count);
            }
            return (academicPerformanceSummary, code);
        }

        //public async Task<(List<string>, int)> GetAllIdsRoleStudentById(List<string> studentIds)
        //{
        //    _apiUserUrl = Environment.GetEnvironmentVariable("USER_SERVICE");
        //    int code = 1003;
        //    List<string> idsRoleStudent = new();
        //    if (!string.IsNullOrEmpty(_apiUserUrl))
        //    {
        //        try
        //        {
        //            string queryString = string.Join("&", studentIds.Select(id => $"userIds={id}"));
        //            var userApiCheckRole = await _httpClient.GetAsync($"{_apiUserUrl}/user/checkRoleUsers/student?{queryString}");
        //            var userApiResponse = await userApiCheckRole.Content.ReadAsStringAsync();
        //            var apiResponse = JsonSerializer.Deserialize<ApiResponse<Dictionary<string, bool>>>(userApiResponse, new JsonSerializerOptions
        //            {
        //                PropertyNameCaseInsensitive = true
        //            });

        //            if (userApiResponse != null && userApiCheckRole.IsSuccessStatusCode && apiResponse != null)
        //            {
        //                code = apiResponse.Code;
        //                if (code == 1000)
        //                {
        //                    foreach (var item in apiResponse.Result)
        //                    {
        //                        if (item.Value)
        //                        {
        //                            idsRoleStudent.Add(item.Key);
        //                        }
        //                    }
        //                }
        //            }
        //        }
        //        catch (Exception ex)
        //        {
        //            code = 1003;
        //        }
        //    }
        //    return (idsRoleStudent, code);
        //}

        //public async Task<(List<UserServiceStudentDto>, int)> GetAllStudentsById(List<string> studentIds)
        //{
        //    _apiUserUrl = Environment.GetEnvironmentVariable("USER_SERVICE");
        //    var studentDtos = new List<UserServiceStudentDto>();
        //    int code = 1003;
        //    if (!string.IsNullOrEmpty(_apiUserUrl))
        //    {
        //        try
        //        {
        //            code = 1003;
        //            string queryString = string.Join("&", studentIds.Select(s => $"ids={s}"));
        //            var userApiGetUserByIds = await _httpClient.GetAsync($"{_apiUserUrl}/user/by-ids?{queryString}");
        //            var userApiResponse = await userApiGetUserByIds.Content.ReadAsStringAsync();
        //            var apiResponse = JsonSerializer.Deserialize<ApiResponse<List<UserServiceStudentDto>>>(userApiResponse, new JsonSerializerOptions
        //            {
        //                PropertyNameCaseInsensitive = true
        //            });

        //            if (userApiResponse != null && userApiGetUserByIds.IsSuccessStatusCode && apiResponse != null)
        //            {
        //                studentDtos = apiResponse.Result;
        //                code = apiResponse.Code;
        //            }
        //        }
        //        catch (Exception ex)
        //        {
        //            code = 1003;
        //        }
        //    }
        //    return (studentDtos, code);
        //}

        /*public async Task<(List<StudentYearlyResultDto>, int)> GetStudentAnnualAcademicResults(string studentId, long schoolYearId, long classId)
        {
            try
            {
                var marks = await _markRepository.GetByConditionAsync(s => s.student_id == studentId && s.school_year_id == schoolYearId && s.class_id == classId);
                var studentYearlyResultDto = new List<StudentYearlyResultDto>();
                int code = marks.Item2;
                if (code == 1000)
                {

                    if (marks.Item1[0].score_average == null || marks.Item1[1].score_average == null || marks.Item1[0].training_result.IsNullOrEmpty() || marks.Item1[1].training_result.IsNullOrEmpty())
                    {
                        code = 1003;
                    }
                    else
                    {
                        var mark_semester_1 = marks.Item1[0];
                        var mark_semester_2 = marks.Item1[1];
                        decimal? averageScore = mark_semester_1.score_average * mark_semester_1.weight + mark_semester_2.score_average * mark_semester_2.weight;
                        string academicPerformance = "";
                        academicPerformance = GetAcademicPerformance(averageScore);

                        var trainingResult = CalculateTrainingResult(mark_semester_1.training_result, mark_semester_2.training_result);


                        return new StudentYearlyResultDto
                        {
                            StudentId = mark_semester_1.student_id,
                            ScoreAverage = (float)averageScore,
                            AcademicPerformance = academicPerformance,
                            TrainingResult = trainingResult
                        };
                    }
                }
                return (studentYearlyResultDto, code);
            }
            catch (Exception ex)
            {
                return (new(), 1001);
            }
        }*/

        /// <summary>
        /// update handle logic
        /// </summary>
        /// <param name="studentId"></param>
        /// <param name="schoolYearId"></param>
        /// <param name="classId"></param>
        /// <returns></returns>
        public async Task<(StudentYearlyResultDto, int)> GetStudentAnnualAcademicResults(string studentId, long schoolYearId, long classId)
        {
            try
            {
                var marks = await _markRepository.GetByConditionAsync(s => s.StudentId == studentId && s.SchoolYearId == schoolYearId && s.ClassId == classId);
                var studentYearlyResultDto = new StudentYearlyResultDto();
                int code = marks.Item2;
                if (code == 1000)
                {
                    if (
                        //marks.Item1[0].ScoreAverage == null || marks.Item1[1].ScoreAverage == null || 
                        marks.Item1[0].Conduct.IsNullOrEmpty() || marks.Item1[1].Conduct.IsNullOrEmpty())
                    {
                        code = 1003;
                    }
                    else
                    {
                        var mark_semester_1 = marks.Item1[0];
                        var mark_semester_2 = marks.Item1[1];
                        decimal? averageScore = 0m;
                            //mark_semester_1.ScoreAverage * mark_semester_1.Weight + mark_semester_2.ScoreAverage * mark_semester_2.Weight;
                        string academicPerformance = "";
                        if (averageScore >= 8.0m && averageScore <= 10.0m)
                        {
                            academicPerformance = "Good";
                        }
                        else if (averageScore >= 6.5m)
                        {
                            academicPerformance = "Fair";
                        }
                        else if (averageScore >= 5.0m)
                        {
                            academicPerformance = "Average";
                        }
                        else
                        {
                            academicPerformance = "Poor";
                        }

                        var conductResult = CalculateTrainingResult(mark_semester_1.Conduct, mark_semester_2.Conduct);


                        studentYearlyResultDto = new StudentYearlyResultDto
                        {
                            StudentId = mark_semester_1.StudentId,
                            ScoreAverage = (decimal)averageScore,
                            AcademicPerformance = academicPerformance,
                            Conduct = conductResult
                        };
                    }
                }
                return (studentYearlyResultDto, code);
            }
            catch (Exception ex)
            {
                return (new(), 1001);
            }
        }

        public string GetAcademicPerformance(decimal averageScore)
        {
            string academicPerformance = "Unknown";
            if (averageScore >= 8.0m && averageScore <= 10.0m)
            {
                academicPerformance = "Good";
            }
            else if (averageScore >= 6.5m)
            {
                academicPerformance = "Fair";
            }
            else if (averageScore >= 5.0m)
            {
                academicPerformance = "Average";
            }
            else
            {
                academicPerformance = "Poor";
            }
            return academicPerformance;
        }

        public string CalculateTrainingResult(string semester1, string semester2)
        {
            if (semester2 == TrainingResultConstants.Excellent)
            {
                if (semester1 == TrainingResultConstants.Excellent || semester1 == TrainingResultConstants.Good)
                    return TrainingResultConstants.Excellent;
                if (semester1 == TrainingResultConstants.Average || semester1 == TrainingResultConstants.Excellent)
                    return TrainingResultConstants.Good;
            }

            if (semester2 == TrainingResultConstants.Good)
            {
                if (semester1 == TrainingResultConstants.Excellent || semester1 == TrainingResultConstants.Good || semester1 == TrainingResultConstants.Average)
                    return TrainingResultConstants.Good;
                if (semester1 == TrainingResultConstants.Poor)
                    return TrainingResultConstants.Average;
            }

            if (semester2 == TrainingResultConstants.Average)
            {
                if (semester1 == TrainingResultConstants.Excellent || semester1 == TrainingResultConstants.Good || semester1 == TrainingResultConstants.Average || semester1 == TrainingResultConstants.Poor)
                    return TrainingResultConstants.Average;
            }

            return TrainingResultConstants.Poor;
        }

        public async Task<(MarkDto, int)> GetMarkBySubjectIdStudentId(long subjectId, string studentId)
        {
            var mark = await _markRepository.GetByConditionAsync(s => s.StudentId == studentId );
            var markDto = _mapper.Map<MarkDto>(mark.Item1);
            return (markDto, mark.Item2);
        }

        public async Task<int> UpdateConductStudent(long markId, string conduct)
        {
            var mark = await _markRepository.GetByIdAsync(markId);
            var code = mark.Item2;
            if(code == 1000)
            {
                mark.Item1.Conduct = conduct;
                return await _markRepository.UpdateAsync(mark.Item1);
            }
            return code;
        }
        
        public Task<int> UpdateMark(long id, MarkCreateUpdateDto markUpdateDto)
        {
            var mark = _mapper.Map<Mark>(markUpdateDto);
            mark.Id = id;
            return _markRepository.UpdateAsync(mark);
        }

        public async Task<(MarkDto, int)> UpdateScore(long id)
        {
            try
            {
                var mark = await _markRepository.GetByIdAsync(id);
                var markDto = new MarkDto();
                int code = mark.Item2;
                if (mark.Item2 == 1000)
                {
                    var componentPoints = await _componentPointRepository.GetByConditionAsync(s => s.MarkSubjectId == mark.Item1.Id);
                    if (componentPoints.Item2 != 1000)
                    {
                        //return (new(), componentPoints.Item2);
                        code = componentPoints.Item2;
                    }
                    decimal totalWeightedScore = 0;
                    decimal totalWeight = 0;
                    string academicPerformance = "";

                    /*foreach (var componentPoint in componentPoints.Item1)
                    {
                        var componentPointMarkType = await _componentPointMarkTypeRepository.GetByConditionAsync(s => s.component_point_id == componentPoint.id);
                        if (componentPointMarkType.Item2 != 1000)
                        {
                            //return (new(), componentPointMarkType.Item2);
                            code = componentPointMarkType.Item2;
                        }
                        var marktypes = await _markTypeRepository.GetByIdAsync(componentPointMarkType.Item1[0].mark_type_id);
                        if (marktypes.Item2 != 1000)
                        {
                            //return (new(), marktypes.Item2);
                            code = marktypes.Item2;
                        }
                        totalWeightedScore += (componentPoint.score * marktypes.Item1.weight);
                        totalWeight += marktypes.Item1.weight;
                    }*/

                    if (totalWeight < 1)
                    {
                        code = 1003;
                    }
                    else if (totalWeightedScore >= 8.0m && totalWeightedScore <= 10.0m)
                    {
                        academicPerformance = "Good";
                    }
                    else if (totalWeightedScore >= 6.5m)
                    {
                        academicPerformance = "Fair";
                    }
                    else if (totalWeightedScore >= 5.0m)
                    {
                        academicPerformance = "Average";
                    }
                    else
                    {
                        academicPerformance = "Poor";
                    }
                    //mark.Item1.ScoreAverage = totalWeightedScore;
                    mark.Item1.AcademicPerformance = academicPerformance;
                    code = await _markRepository.UpdateAsync(mark.Item1);
                    markDto = _mapper.Map<MarkDto>(mark.Item1);
                }
                return (markDto, code);
            }
            catch (Exception ex)
            {
                return (new(), 1001);
            }
        }
    }
}
