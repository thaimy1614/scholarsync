using AutoMapper;
using Microsoft.AspNetCore.Mvc.RazorPages;
using subject_service.Common.Pagination;
using subject_service.Dtos.Subject;
using subject_service.HttpApi;
using subject_service.Models;
using subject_service.Repositories.Implementations;
using subject_service.Repositories.Interfaces;
using subject_service.Services.Interfaces;
using System.Text.Json;

namespace subject_service.Services.Implementations
{
    public class SubjectService : ISubjectService
    {
        private readonly ISubjectRepository _subjectRepository;
        private readonly ITeacherSubjectClassService _teacherSubjectClassService;
        private readonly IUserClientService _userClientService;
        private readonly IMapper _mapper;

        public SubjectService(ISubjectRepository subjectRepository, ITeacherSubjectClassService teacherSubjectClassService, IUserClientService userClientService, IMapper mapper) { 
            _subjectRepository = subjectRepository;
            _teacherSubjectClassService = teacherSubjectClassService;
            _userClientService = userClientService;
            _mapper = mapper;
        }

        public async Task<(List<string>, int)> AddSubject(List<SubjectCreateUpdateDto> subjectCreateDtos)
        {
            List<string> errorSubject = new();
            int code = 1000;
            var entities = _mapper.Map<List<Subject>>(subjectCreateDtos);

            for (int i = 0; i < entities.Count; i++)
            {
                var codeResult = await _subjectRepository.AddAsync(entities[i]);
                if (codeResult != 1000)
                {
                    errorSubject.Add(i.ToString());
                }
            }
            //if (errorSubject.Count == subjectCreateDtos.Count)
            //{
            //    code = 1002;
            //}
            return (errorSubject, code);
        }

        public async Task<int> DeleteSubject(long id)
        {
            return await _subjectRepository.DeleteAsync(id);
        }

        public async Task<(PagedResult<SubjectWithDeletedDto>, int)> GetAllSubjects(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await _subjectRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, true);
            var subjectWithDeletedDtos = _mapper.Map<PagedResult<SubjectWithDeletedDto>>(result.Item1);
            return (subjectWithDeletedDtos, result.Item2);
        }
        
        public async Task<(PagedResult<SubjectDto>, int)> GetAllExistingSubjects(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await _subjectRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, false);
            var subjectDtos = _mapper.Map<PagedResult<SubjectDto>>(result.Item1);
            return (subjectDtos, result.Item2);
        }
        
        public async Task<(PagedResult<SubjectDto>, int)> SearchSubjects(string? keyword, int page, int pageSize, string sortColumn, bool isDescending, bool includeDeleted)
        {
            var result = await _subjectRepository.SearchAsync(keyword, page, pageSize, sortColumn, isDescending, includeDeleted);
            var subjectDtos = _mapper.Map<PagedResult<SubjectDto>>(result.Item1);
            return (subjectDtos, result.Item2);
        }

        public async Task<(List<SubjectDto>, int)> GetAllSubjectsByTeacherId(string teacherId)
        {
            //_apiUserUrl = Environment.GetEnvironmentVariable("USER_SERVICE");
            //int code = 1003;
            /*if (!string.IsNullOrEmpty(_apiUserUrl))
            {
                try
                {
                    string queryString = $"userIds={teacherId}";
                    var userApiCheckRole = await _httpClient.GetAsync($"{_apiUserUrl}/user/checkRoleUsers/teacher?{queryString}");
                    var userApiResponse = await userApiCheckRole.Content.ReadAsStringAsync();
                    var apiResponse = JsonSerializer.Deserialize<ApiResponse<Dictionary<string, bool>>>(userApiResponse, new JsonSerializerOptions
                    {
                        PropertyNameCaseInsensitive = true
                    });

                    if(userApiResponse != null && userApiCheckRole.IsSuccessStatusCode && apiResponse != null)
                    {
                        (List<Subject>, int) item = new();
                        code = apiResponse.Code;

                        foreach (var isTeacher in apiResponse.Result)
                        {
                            if (!isTeacher.Value)
                            {
                                code = 1004;
                            }
                        }

                    }
                }
                catch (Exception ex)
                {
                    code = 1003;
                }
            }*/
            var subjectDtos = new List<SubjectDto>();
            var userIds = new List<string> { teacherId };
            var teacherIds = await _userClientService.GetAllIdsRoleTeacherById(userIds);
            int code = teacherIds.Item2;

            if (code == 1000)
            {
                var subjectIds = await _teacherSubjectClassService.GetAllSubjectIdsByTeacherId(teacherId);
                code = subjectIds.Item2;
                if (code == 1000)
                {
                    var subjects = await _subjectRepository.GetByConditionAsync(s => subjectIds.Item1.Contains(s.Id));
                    code = subjects.Item2;
                    subjectDtos = _mapper.Map<List<SubjectDto>>(subjects.Item1);
                }
            }
            return (subjectDtos, code);
        }

        public async Task<(SubjectDto, int)> GetSubjectById(long id)
        {
            var result = await _subjectRepository.GetByIdAsync(id);
            var subjectDto = _mapper.Map<SubjectDto>(result.Item1);
            return (subjectDto, result.Item2);
        }
        
        public async Task<(string, int)> GetSubjectNameById(long id)
        {
            var result = await _subjectRepository.GetByIdAsync(id);
            //var subjectDto = _mapper.Map<SubjectDto>(result.Item1);
            return (result.Item1.Name, result.Item2);
        }
        
        public async Task<(List<SubjectDto>, int)> GetAllSubjectsByIds(List<long> ids)
        {
            var result = await _subjectRepository.GetByConditionAsync(s => ids.Contains(s.Id));
            var subjectDto = _mapper.Map<List<SubjectDto>>(result.Item1);
            return (subjectDto, result.Item2);
        }

        public Task<int> UpdateSubject(long id, SubjectCreateUpdateDto subjectUpdateDto)
        {
            var entity = _mapper.Map<Subject>(subjectUpdateDto);
            entity.Id = id;
            return _subjectRepository.UpdateAsync(entity);
        }
    }
}
