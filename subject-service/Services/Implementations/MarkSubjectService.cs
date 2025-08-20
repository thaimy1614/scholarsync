using AutoMapper;
using Microsoft.AspNetCore.Mvc.RazorPages;
using subject_service.Common.Pagination;
using subject_service.Dtos.MarkSubject;
using subject_service.Models;
using subject_service.Repositories.Interfaces;
using subject_service.Services.Interfaces;

namespace subject_service.Services.Implementations
{
    public class MarkSubjectService : IMarkSubjectService
    {
        private readonly IMarkSubjectRepository _subjectMarkRepository;
        private readonly IMapper _mapper;
        private readonly HttpClient _httpClient;

        public MarkSubjectService(IMarkSubjectRepository subjectMarkRepository, IMapper mapper, HttpClient httpClient)
        {
            _subjectMarkRepository = subjectMarkRepository;
            _mapper = mapper;
            _httpClient = httpClient;
        }

        public async Task<int> AddSubjectMark(MarkSubjectCreateUpdateDto subjectMarkCreateDto)
        {
            var entity = _mapper.Map<MarkSubject>(subjectMarkCreateDto);
            return await _subjectMarkRepository.AddAsync(entity);
        }

        public Task<int> DeleteSubjectMark(long id)
        {
            return _subjectMarkRepository.DeleteAsync(id);
        }

        public async Task<(PagedResult<MarkSubjectWithDeletedDto>, int)> GetAllSubjectMarks(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await _subjectMarkRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, true);
            var subjectMarkWithDeletedDtos = _mapper.Map<PagedResult<MarkSubjectWithDeletedDto>>(result.Item1);
            return (subjectMarkWithDeletedDtos, result.Item2);
        }

        public async Task<(PagedResult<MarkSubjectDto>, int)> GetAllExistingSubjectMarks(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await _subjectMarkRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, false);
            var subjectMarkDtos = _mapper.Map<PagedResult<MarkSubjectDto>>(result.Item1);
            return (subjectMarkDtos, result.Item2);
        }

        public async Task<(MarkSubjectDto, int)> GetSubjectMarkById(long id)
        {
            var result = await _subjectMarkRepository.GetByIdAsync(id);
            var subjectMarkDto = _mapper.Map<MarkSubjectDto>(result.Item1);
            return (subjectMarkDto, result.Item2);
        }

        public Task<int> UpdateSubjectMark(long id, MarkSubjectCreateUpdateDto subjectMarkUpdateDto)
        {
            var entity = _mapper.Map<MarkSubject>(subjectMarkUpdateDto);
            entity.Id = id;
            return _subjectMarkRepository.UpdateAsync(entity);
        }
    }
}