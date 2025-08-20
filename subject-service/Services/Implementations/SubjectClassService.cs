using AutoMapper;
using Microsoft.AspNetCore.Mvc.RazorPages;
using subject_service.Common.Pagination;
using subject_service.Dtos.SubjectClass;
using subject_service.Models;
using subject_service.Repositories.Interfaces;
using subject_service.Services.Interfaces;

namespace subject_service.Services.Implementations
{
    public class SubjectClassService : ISubjectClassService
    {
        private readonly ISubjectClassRepository _subjectClassRepository;
        private readonly IMapper _mapper;
        //private readonly HttpClient _httpClient;

        public SubjectClassService(ISubjectClassRepository subjectClassRepository, IMapper mapper)
        {
            _subjectClassRepository = subjectClassRepository;
            _mapper = mapper;
            //_httpClient = httpClient;
        }

        public async Task<int> AddSubjectClass(SubjectClassCreateUpdateDto subjectClassCreateDto)
        {
            var entity = _mapper.Map<SubjectClass>(subjectClassCreateDto);
            return await _subjectClassRepository.AddAsync(entity);
        }
        
        public async Task<(List<long>, int)> AssignSubjectToClasses(long subjectId, List<long> classIds)
        {
            var errorClassIds = new List<long>();
            var (subjectClasses, code) = await _subjectClassRepository.GetByConditionAsync(s => s.SubjectId == subjectId);
            if(code == 1000)
            {
                var baseClassIds = subjectClasses.Select(s => s.ClassId);
                var itemAdd = classIds.Except(baseClassIds);
                var itemRemove = baseClassIds.Except(classIds);
                foreach (var classId in itemAdd)
                {
                    var entity = new SubjectClass
                    {
                        SubjectId = subjectId,
                        ClassId = classId
                    };
                    int codeResult;
                    codeResult = await _subjectClassRepository.AddAsync(entity);
                    if (codeResult != 1000)
                    {
                        errorClassIds.Add(classId);
                    }
                }
                foreach (var classId in itemRemove)
                {
                    int codeResult;
                    codeResult = await _subjectClassRepository.DeleteAsync(subjectId, classId);
                    if (codeResult != 1000)
                    {
                        errorClassIds.Add(classId);
                    }
                }

            }    
            return (errorClassIds, 1000);
        }

        public Task<int> DeleteSubjectClass(long subjectId, long classId)
        {
            return _subjectClassRepository.DeleteAsync(subjectId, classId);
        }

        public async Task<(PagedResult<SubjectClassWithDeletedDto>, int)> GetAllSubjectClasses(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await _subjectClassRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, true);
            var subjectClassWithDeletedDtos = _mapper.Map<PagedResult<SubjectClassWithDeletedDto>>(result.Item1);
            return (subjectClassWithDeletedDtos, result.Item2);
        }

        public async Task<(PagedResult<SubjectClassDto>, int)> GetAllExistingSubjectClasses(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await _subjectClassRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, false);
            var subjectClassDtos = _mapper.Map<PagedResult<SubjectClassDto>>(result.Item1);
            return (subjectClassDtos, result.Item2);
        }

        public async Task<(SubjectClassDto, int)> GetSubjectClassById(long subjectId, long classId)
        {
            var result = await _subjectClassRepository.GetByIdAsync(subjectId, classId);
            var subjectClassDto = _mapper.Map<SubjectClassDto>(result.Item1);
            return (subjectClassDto, result.Item2);
        }

        public Task<int> UpdateSubjectClass(SubjectClassCreateUpdateDto subjectClassUpdateDto)
        {
            var entity = _mapper.Map<SubjectClass>(subjectClassUpdateDto);
            return _subjectClassRepository.UpdateAsync(entity);
        }
    }
}