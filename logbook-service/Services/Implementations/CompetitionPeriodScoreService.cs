using AutoMapper;
using Microsoft.AspNetCore.Mvc.RazorPages;
using logbook_service.Models;
using logbook_service.Models.Dtos;
using logbook_service.Repositories.Interfaces;
using logbook_service.Services.Interfaces;

namespace logbook_service.Services.Implementations
{
    public class CompetitionPeriodScoreService : ICompetitionPeriodScoreService
    {
        private readonly ICompetitionPeriodScoreRepository _competitionPeriodScoreRepository;
        private readonly IMapper _mapper;
        private readonly HttpClient _httpClient;

        public CompetitionPeriodScoreService(ICompetitionPeriodScoreRepository competitionPeriodScoreRepository, IMapper mapper, HttpClient httpClient)
        {
            _competitionPeriodScoreRepository = competitionPeriodScoreRepository;
            _mapper = mapper;
            _httpClient = httpClient;
        }

        public async Task<int> AddCompetitionPeriodScore(CompetitionPeriodScoreCreateUpdateDto competitionPeriodScoreCreateDto)
        {
            var entity = _mapper.Map<CompetitionPeriodScore>(competitionPeriodScoreCreateDto);
            return await _competitionPeriodScoreRepository.AddAsync(entity);
        }

        public Task<int> DeleteCompetitionPeriodScore(long id)
        {
            return _competitionPeriodScoreRepository.DeleteAsync(id);
        }

        public async Task<(PagedResult<CompetitionPeriodScoreWithDeletedDto>, int)> GetAllCompetitionPeriodScores(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await _competitionPeriodScoreRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, true);
            var competitionPeriodScoreWithDeletedDtos = _mapper.Map<PagedResult<CompetitionPeriodScoreWithDeletedDto>>(result.Item1);
            return (competitionPeriodScoreWithDeletedDtos, result.Item2);
        }

        public async Task<(PagedResult<CompetitionPeriodScoreDto>, int)> GetAllExistingCompetitionPeriodScores(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await _competitionPeriodScoreRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, false);
            var competitionPeriodScoreDtos = _mapper.Map<PagedResult<CompetitionPeriodScoreDto>>(result.Item1);
            return (competitionPeriodScoreDtos, result.Item2);
        }

        public async Task<(CompetitionPeriodScoreDto, int)> GetCompetitionPeriodScoreById(long id)
        {
            var result = await _competitionPeriodScoreRepository.GetByIdAsync(id);
            var competitionPeriodScoreDto = _mapper.Map<CompetitionPeriodScoreDto>(result.Item1);
            return (competitionPeriodScoreDto, result.Item2);
        }

        public Task<int> UpdateCompetitionPeriodScore (long id, CompetitionPeriodScoreCreateUpdateDto competitionPeriodScoreUpdateDto)
        {
            var entity = _mapper.Map<CompetitionPeriodScore>(competitionPeriodScoreUpdateDto);
            entity.id = id;
            return _competitionPeriodScoreRepository.UpdateAsync(entity);
        }
    }
}