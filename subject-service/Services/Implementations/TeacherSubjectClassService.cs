using AutoMapper;
using Microsoft.IdentityModel.Tokens;
using subject_service.Common.Pagination;
using subject_service.Dtos.Class;
using subject_service.Dtos.Teacher;
using subject_service.Dtos.TeacherSubjectClass;
using subject_service.HttpApi;
using subject_service.Models;
using subject_service.Repositories.Implementations;
using subject_service.Repositories.Interfaces;
using subject_service.Services.Interfaces;
using System.Collections;
using System.Text.Json;

namespace subject_service.Services.Implementations
{
    public class TeacherSubjectClassService : ITeacherSubjectClassService
    {
        private readonly ITeacherSubjectClassRepository _teacherSubjectClassRepository;
        private readonly ISubjectRepository _subjectRepository;
        private readonly IUserClientService _userClientService;
        private readonly ISchoolClientService _schoolClientService;
        private readonly IMapper _mapper;

        public TeacherSubjectClassService(ITeacherSubjectClassRepository teacherSubjectClassRepository, ISubjectRepository subjectRepository, IMapper mapper, IUserClientService userClientService, ISchoolClientService schoolClientService)
        {
            _teacherSubjectClassRepository = teacherSubjectClassRepository;
            _subjectRepository = subjectRepository;
            _userClientService = userClientService;
            _schoolClientService = schoolClientService;
            _mapper = mapper;
        }

        public async Task<(List<string>, int)> AddTeacherSubjectClass(List<TeacherSubjectClassCreateUpdateDto> subjectTeacherCreateDtos)
        {
            List<string> userIds = new List<string>();
            userIds.AddRange(subjectTeacherCreateDtos.Select(s => s.TeacherId));
            var teacherIds = await _userClientService.GetAllIdsRoleTeacherById(userIds);
            int code = teacherIds.Item2;
            List<string> errorUser = new();
            if(code == 1000)
            {
                errorUser = userIds.Except(teacherIds.Item1).ToList();
                subjectTeacherCreateDtos = subjectTeacherCreateDtos.Where(s => !errorUser.Contains(s.TeacherId)).ToList();
                var teacherSubjectClasses = _mapper.Map<List<TeacherSubjectClass>>(subjectTeacherCreateDtos);
                foreach (var item in teacherSubjectClasses)
                {
                    var codeResult = await _teacherSubjectClassRepository.AddAsync(item);
                    if (codeResult != 1000)
                    {
                        errorUser.Add(item.TeacherId);
                    }
                }
            }   
            return (errorUser, code);
        }

        public Task<int> DeleteTeacherSubjectClass(string teacherId, long subjectId, long classId)
        {
            return _teacherSubjectClassRepository.DeleteAsync(teacherId, subjectId, classId);
        }

        public async Task<(PagedResult<TeacherSubjectClassWithDeletedDto>, int)> GetAllTeacherSubjectClasses(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await _teacherSubjectClassRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, true);
            var teacherSubjectClassWithDeletedDtos = _mapper.Map<PagedResult<TeacherSubjectClassWithDeletedDto>>(result.Item1);
            return (teacherSubjectClassWithDeletedDtos, result.Item2);
        }

        public async Task<(PagedResult<TeacherSubjectClassDto>, int)> GetAllExistingTeacherSubjectClasses(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await _teacherSubjectClassRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, false);
            var teacherSubjectClassDtos = _mapper.Map<PagedResult<TeacherSubjectClassDto>>(result.Item1);
            return (teacherSubjectClassDtos, result.Item2);
        }

        //public async Task<(IEnumerable<TeacherSubjectClassDto>, int)> GetAllTeacherSubjectClasses()
        //{
        //    var result = await _teacherSubjectClassRepository.GetAllAsync();
        //    var teacherSubjectClassDtos = _mapper.Map<List<TeacherSubjectClassDto>>(result.Item1);
        //    return (teacherSubjectClassDtos, result.Item2);
        //}

        //public async Task<(List<string>, int)> GetAllTeacherIdsBySubjectId(long subjectId)
        //{
        //    var teacherIds = new List<string>();
        //    var subjectsTeachers = await _subjectTeacherRepository.GetByConditionAsync(s => s.subject_id == subjectId);
        //    if (!subjectsTeachers.Item1.IsNullOrEmpty())
        //    {
        //        teacherIds = subjectsTeachers.Item1.Select(s => s.teacher_id).ToList();
        //    }
        //    return new
        //    (
        //        teacherIds,
        //        subjectsTeachers.Item2
        //    );
        //}

        public async Task<(List<TeacherSubjectClassDetailDto>, int)> GetAllTeacherSubjectClassDetailByClassId(long classId)
        {
            var teacherSubjectClassDetailDtos = new List<TeacherSubjectClassDetailDto>();
            var (teacherSubjectClasses, code) = await _teacherSubjectClassRepository.GetByConditionAsync(s => s.ClassId == classId);
            if (code == 1000)
            {
                var teacherIds = teacherSubjectClasses.Select(x => x.TeacherId).Distinct().ToList();
                var subjectIds = teacherSubjectClasses.Select(x => x.SubjectId).Distinct().ToList();
                var classIds = teacherSubjectClasses.Select(x => x.ClassId).Distinct().ToList();
                var (teacherInfos, codeResultTeacher) = await _userClientService.GetAllTeachersById(teacherIds);
                var (subjectInfos, codeResultSubject) = await _subjectRepository.GetByConditionAsync(s => subjectIds.Contains(s.Id));
                var classInfos = new List<ClassDto>();

                foreach(var id in classIds)
                {
                    var (classes, codeResultClass) = await _schoolClientService.GetClassNameByClassId(classId);
                    if(codeResultClass == 1000)
                    {
                        classInfos.Add(classes);
                    }
                }
                var teacherDict = teacherInfos.ToDictionary(t => t.UserId);
                var classDict = classInfos.ToDictionary(c => c.ClassId);
                var subjectDict = subjectInfos.ToDictionary(s => s.Id);

                teacherSubjectClassDetailDtos = teacherSubjectClasses.Select(item =>
                {
                    teacherDict.TryGetValue(item.TeacherId, out var @teacher);
                    classDict.TryGetValue(item.ClassId, out var @class);
                    subjectDict.TryGetValue(item.SubjectId, out var @subject);

                    return new TeacherSubjectClassDetailDto
                    {
                        TeacherId = item.TeacherId,
                        FullName = @teacher?.FullName,
                        Image = @teacher?.Image,
                        Email = @teacher?.Email,
                        PhoneNumber = @teacher?.PhoneNumber,
                        Address = @teacher?.Address,
                        Gender = @teacher?.Gender,

                        SubjectId = item.SubjectId,
                        SubjectName = @subject?.Name,
                        WeeklySlots = (int)(@subject?.WeeklySlots),
                        MaxSlotsPerSession = (int)(@subject?.MaxSlotsPerSession),
                        PreferConsecutive = @subject.PreferConsecutive,
                        IsMainSubject = @subject.IsMainSubject,
                        IsScoreable = @subject.IsScoreable,
                        IsEvaluateWithScore = @subject.IsEvaluateWithScore,
                        SpecialSlot = @subject.SpecialSlot,
                        SpecialRoom = @subject.SpecialRoom,
                        SchoolYearId = (long)(@subject?.SchoolYearId),
                        ClassId = item.ClassId,
                        ClassName = @class?.ClassName,
                        HomeroomTeacher = @class.Teacher
                    };
                }).ToList();
            }
            return new
            (
                teacherSubjectClassDetailDtos,
                code
            );
        }

        //public async Task<(ClassDto, int)> GetClassNameByClassId(long classId)
        //{
        //    _apiSchoolUrl = Environment.GetEnvironmentVariable("SCHOOL_SERVICE");
        //    int code = 1003;
        //    var classDto = new ClassDto();
        //    if (!string.IsNullOrEmpty(_apiSchoolUrl))
        //    {
        //        try
        //        {
        //            //string queryString = string.Join("&", teacherIds.Select(id => $"userIds={id}"));
        //            var schoolApiCheckRole = await _httpClient.GetAsync($"{_apiSchoolUrl}/school/class/get-class-by-id/{classId}");
        //            var schoolApiResponse = await schoolApiCheckRole.Content.ReadAsStringAsync();
        //            var apiResponse = JsonSerializer.Deserialize<ApiResponse<ClassDto>>(schoolApiResponse, new JsonSerializerOptions
        //            {
        //                PropertyNameCaseInsensitive = true
        //            });

        //            if (schoolApiResponse != null && schoolApiCheckRole.IsSuccessStatusCode && apiResponse != null)
        //            {
        //                code = apiResponse.Code;
        //                classDto = apiResponse.Result;
        //            }
        //        }
        //        catch (Exception ex)
        //        {
        //            code = 1003;
        //        }
        //    }
        //    return (classDto, code);
        //}

        public async Task<(List<long>, int)> GetAllSubjectIdsByTeacherId(string teacherId)
        {
            //_apiUserUrl = Environment.GetEnvironmentVariable("USER_SERVICE");
            /*int code = 1004;*/
            //var subjectIds = new List<long>();
            //if (!string.IsNullOrEmpty(_apiUserUrl))
            //{
            //    try
            //    {
            //        string queryString = string.Join("&", $"userIds={teacherId}");
            //        var userApiCheckRole = await _httpClient.GetAsync($"{_apiUserUrl}/user/checkRoleUsers/teacher?{queryString}");
            //        var userApiResponse = await userApiCheckRole.Content.ReadAsStringAsync();
            //        var apiResponse = JsonSerializer.Deserialize<ApiResponse<Dictionary<string, bool>>>(userApiResponse, new JsonSerializerOptions
            //        {
            //            PropertyNameCaseInsensitive = true
            //        });

            //        if (userApiResponse != null && userApiCheckRole.IsSuccessStatusCode && apiResponse != null)
            //        {
            //            code = apiResponse.Code;
            //            if (code == 1000)
            //            {
            //                foreach (var item in apiResponse.Result)
            //                {
            //                    if (!item.Value)
            //                    {
            //                        code = 1003;    
            //                    }
            //                }
            //            }
            //        }
            //    }
            //    catch (Exception ex)
            //    {
            //        code = 1004;
            //    }
            //}

            var userIds = new List<string> { teacherId };
            var subjectIds = new List<long>();
            var teacherIds =  await _userClientService.GetAllIdsRoleTeacherById(userIds);
            int code = teacherIds.Item2;

            if (code == 1000)
            {
                var subjectsTeachers = await _teacherSubjectClassRepository.GetByConditionAsync(s => s.TeacherId == teacherId);

                if (!subjectsTeachers.Item1.IsNullOrEmpty())
                {
                    subjectIds = subjectsTeachers.Item1.Select(s => s.SubjectId).ToList();
                    code = subjectsTeachers.Item2;
                }
            }

            return new
            (
                subjectIds,
                code
            );
        }

        public async Task<(List<TeacherDto>, int)> GetAllTeachersBySubjectId(long subjectId)
        {
            var teacherDtos = new List<TeacherDto>();
            var userIds = new List<string>();
            var subjectsTeachers = await _teacherSubjectClassRepository.GetByConditionAsync(s => s.SubjectId == subjectId);
            if (!subjectsTeachers.Item1.IsNullOrEmpty())
            {
                userIds = subjectsTeachers.Item1.Select(s => s.TeacherId).ToList();
            }
            int code = subjectsTeachers.Item2;
            if (code == 1000)
            {
                var teacherIds = await _userClientService.GetAllIdsRoleTeacherById(userIds);
                code = teacherIds.Item2;
                if(code == 1000)
                {
                    var teacherByIds = await _userClientService.GetAllTeachersById(teacherIds.Item1);
                    teacherDtos = teacherByIds.Item1;
                    code = teacherByIds.Item2;
                }
                /*_apiUserUrl = Environment.GetEnvironmentVariable("USER_SERVICE");
                if (!string.IsNullOrEmpty(_apiUserUrl))
                {
                    try
                    {
                        code = 1003;
                        string queryString = string.Join("&", teacherIds.Select(s => $"ids={s}"));
                        var userApiGetUserByIds = await _httpClient.GetAsync($"{_apiUserUrl}/user/by-ids?{queryString}");
                        var userApiResponse = await userApiGetUserByIds.Content.ReadAsStringAsync();
                        var apiResponse = JsonSerializer.Deserialize<ApiResponse<List<UserServiceTeacherDto>>>(userApiResponse, new JsonSerializerOptions
                        {
                            PropertyNameCaseInsensitive = true
                        });

                        if (userApiResponse != null && userApiGetUserByIds.IsSuccessStatusCode && apiResponse != null)
                        {
                            teacherDtos = apiResponse.Result;
                            code = apiResponse.Code;
                        }
                    }
                    catch (Exception ex)
                    {
                        code = 1003;
                    }
                }*/
            }
            return (teacherDtos, code);
        }

        //public async Task<(List<UserServiceTeacherDto>, int)> GetAllTeachersById(List<string> teacherIds)
        //{
        //    _apiUserUrl = Environment.GetEnvironmentVariable("USER_SERVICE");
        //    var teacherDtos = new List<UserServiceTeacherDto>();
        //    int code = 1003;
        //    if (!string.IsNullOrEmpty(_apiUserUrl))
        //    {
        //        try
        //        {
        //            code = 1003;
        //            string queryString = string.Join("&", teacherIds.Select(s => $"ids={s}"));
        //            var userApiGetUserByIds = await _httpClient.GetAsync($"{_apiUserUrl}/user/by-ids?{queryString}");
        //            var userApiResponse = await userApiGetUserByIds.Content.ReadAsStringAsync();
        //            var apiResponse = JsonSerializer.Deserialize<ApiResponse<List<UserServiceTeacherDto>>>(userApiResponse, new JsonSerializerOptions
        //            {
        //                PropertyNameCaseInsensitive = true
        //            });

        //            if (userApiResponse != null && userApiGetUserByIds.IsSuccessStatusCode && apiResponse != null)
        //            {
        //                teacherDtos = apiResponse.Result;
        //                code = apiResponse.Code;
        //            }
        //        }
        //        catch (Exception ex)
        //        {
        //            code = 1003;
        //        }
        //    }
        //    return (teacherDtos, code);
        //}

        //public async Task<(List<string>, int)> GetAllIdsRoleTeacherById(List<string> teacherIds)
        //{
        //    _apiUserUrl = Environment.GetEnvironmentVariable("USER_SERVICE");
        //    int code = 1003;
        //    List<string> idsRoleTeacher = new();
        //    if (!string.IsNullOrEmpty(_apiUserUrl))
        //    {
        //        try
        //        {
        //            string queryString = string.Join("&", teacherIds.Select(id => $"userIds={id}"));
        //            var userApiCheckRole = await _httpClient.GetAsync($"{_apiUserUrl}/user/checkRoleUsers/teacher?{queryString}");
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
        //                            idsRoleTeacher.Add(item.Key);
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
        //    return (idsRoleTeacher, code);
        //}

        public async Task<(bool, int)> CheckTeacherSubjectClassExists(string teacherId, long subjectId, long classId)
        {
            var (teacherSubjectClass, codeResult) = await _teacherSubjectClassRepository.GetByIdAsync(teacherId, subjectId, classId);
            var isExist = false;
            int code = 1000;
            if (codeResult == 1000)
            {
                isExist = true;
                //var classes = teacherSubjectClasses.Item1.Select(s => s.ClassId);
                //foreach (var classId in classIds)
                //{
                //    var isExist = false;
                //    if (classes.Contains(classId))
                //    {
                //        isExist = true;
                //    }
                //    checkClassExists.Add(classId, isExist);
                //}
            }
            else if(code == 1001)
            {
                code = 1001;
            }
            return (isExist, code);
        }

        public async Task<(TeacherSubjectClassDto, int)> GetTeacherSubjectClassById(string teacherId, long subjectId, long classId)
        {
            var result = await _teacherSubjectClassRepository.GetByIdAsync(teacherId, subjectId, classId);
            var teacherSubjectClassDto = _mapper.Map<TeacherSubjectClassDto>(result.Item1);
            return (teacherSubjectClassDto, result.Item2);
        }    

        public Task<int> UpdateTeacherSubjectClass(TeacherSubjectClassCreateUpdateDto teacherSubjectClassUpdateDto)
        {
            var teacherSubjectClass = _mapper.Map<TeacherSubjectClass>(teacherSubjectClassUpdateDto);
            return _teacherSubjectClassRepository.UpdateAsync(teacherSubjectClass);
        }
    }
}
