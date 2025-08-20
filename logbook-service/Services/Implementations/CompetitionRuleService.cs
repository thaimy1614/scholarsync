using AutoMapper;
using Microsoft.AspNetCore.Mvc.RazorPages;
using logbook_service.Models;
using logbook_service.Models.Dtos;
using logbook_service.Repositories.Interfaces;
using logbook_service.Services.Interfaces;

namespace logbook_service.Services.Implementations
{
    public class CompetitionRuleService : ICompetitionRuleService
    {
        private readonly ICompetitionRuleRepository _competitionRuleRepository;
        private readonly IMapper _mapper;
        private readonly HttpClient _httpClient;

        public CompetitionRuleService(ICompetitionRuleRepository competitionRuleRepository, IMapper mapper, HttpClient httpClient)
        {
            _competitionRuleRepository = competitionRuleRepository;
            _mapper = mapper;
            _httpClient = httpClient;
        }

        public async Task<int> AddCompetitionRule(CompetitionRuleCreateUpdateDto competitionRuleCreateDto)
        {
            var entity = _mapper.Map<CompetitionRule>(competitionRuleCreateDto);
            return await _competitionRuleRepository.AddAsync(entity);
        }

        public Task<int> DeleteCompetitionRule(long id)
        {
            return _competitionRuleRepository.DeleteAsync(id);
        }

        public async Task<(PagedResult<CompetitionRuleWithDeletedDto>, int)> GetAllCompetitionRules(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await _competitionRuleRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, true);
            var competitionRuleWithDeletedDtos = _mapper.Map<PagedResult<CompetitionRuleWithDeletedDto>>(result.Item1);
            return (competitionRuleWithDeletedDtos, result.Item2);
        }

        public async Task<(PagedResult<CompetitionRuleDto>, int)> GetAllExistingCompetitionRules(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await _competitionRuleRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, false);
            var competitionRuleDtos = _mapper.Map<PagedResult<CompetitionRuleDto>>(result.Item1);
            return (competitionRuleDtos, result.Item2);
        }

        public async Task<(CompetitionRuleDto, int)> GetCompetitionRuleById(long id)
        {
            var result = await _competitionRuleRepository.GetByIdAsync(id);
            var competitionRuleDto = _mapper.Map<CompetitionRuleDto>(result.Item1);
            return (competitionRuleDto, result.Item2);
        }

        public Task<int> UpdateCompetitionRule (long id, CompetitionRuleCreateUpdateDto competitionRuleUpdateDto)
        {
            var entity = _mapper.Map<CompetitionRule>(competitionRuleUpdateDto);
            entity.id = id;
            return _competitionRuleRepository.UpdateAsync(entity);
        }
    }
}