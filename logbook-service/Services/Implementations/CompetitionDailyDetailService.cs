using AutoMapper;
using Microsoft.AspNetCore.Mvc.RazorPages;
using logbook_service.Models;
using logbook_service.Models.Dtos;
using logbook_service.Repositories.Interfaces;
using logbook_service.Services.Interfaces;

namespace logbook_service.Services.Implementations
{
    public class CompetitionDailyDetailService : ICompetitionDailyDetailService
    {
        private readonly ICompetitionDailyDetailRepository _competitionDailyDetailRepository;
        private readonly IMapper _mapper;
        private readonly HttpClient _httpClient;

        public CompetitionDailyDetailService(ICompetitionDailyDetailRepository competitionDailyDetailRepository, IMapper mapper, HttpClient httpClient)
        {
            _competitionDailyDetailRepository = competitionDailyDetailRepository;
            _mapper = mapper;
            _httpClient = httpClient;
        }

        public async Task<int> AddCompetitionDailyDetail(CompetitionDailyDetailCreateUpdateDto competitionDailyDetailCreateDto)
        {
            var entity = _mapper.Map<CompetitionDailyDetail>(competitionDailyDetailCreateDto);
            return await _competitionDailyDetailRepository.AddAsync(entity);
        }

        public Task<int> DeleteCompetitionDailyDetail(long id)
        {
            return _competitionDailyDetailRepository.DeleteAsync(id);
        }

        public async Task<(PagedResult<CompetitionDailyDetailWithDeletedDto>, int)> GetAllCompetitionDailyDetails(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await _competitionDailyDetailRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, true);
            var competitionDailyDetailWithDeletedDtos = _mapper.Map<PagedResult<CompetitionDailyDetailWithDeletedDto>>(result.Item1);
            return (competitionDailyDetailWithDeletedDtos, result.Item2);
        }

        public async Task<(PagedResult<CompetitionDailyDetailDto>, int)> GetAllExistingCompetitionDailyDetails(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await _competitionDailyDetailRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, false);
            var competitionDailyDetailDtos = _mapper.Map<PagedResult<CompetitionDailyDetailDto>>(result.Item1);
            return (competitionDailyDetailDtos, result.Item2);
        }

        public async Task<(CompetitionDailyDetailDto, int)> GetCompetitionDailyDetailById(long id)
        {
            var result = await _competitionDailyDetailRepository.GetByIdAsync(id);
            var competitionDailyDetailDto = _mapper.Map<CompetitionDailyDetailDto>(result.Item1);
            return (competitionDailyDetailDto, result.Item2);
        }

        public Task<int> UpdateCompetitionDailyDetail (long id, CompetitionDailyDetailCreateUpdateDto competitionDailyDetailUpdateDto)
        {
            var entity = _mapper.Map<CompetitionDailyDetail>(competitionDailyDetailUpdateDto);
            entity.id = id;
            return _competitionDailyDetailRepository.UpdateAsync(entity);
        }
    }
}