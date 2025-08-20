using AutoMapper;
using Microsoft.AspNetCore.Mvc.RazorPages;
using logbook_service.Models;
using logbook_service.Models.Dtos;
using logbook_service.Repositories.Interfaces;
using logbook_service.Services.Interfaces;

namespace logbook_service.Services.Implementations
{
    public class CompetitionDailyRecordService : ICompetitionDailyRecordService
    {
        private readonly ICompetitionDailyRecordRepository _competitionDailyRecordRepository;
        private readonly IMapper _mapper;
        private readonly HttpClient _httpClient;

        public CompetitionDailyRecordService(ICompetitionDailyRecordRepository competitionDailyRecordRepository, IMapper mapper, HttpClient httpClient)
        {
            _competitionDailyRecordRepository = competitionDailyRecordRepository;
            _mapper = mapper;
            _httpClient = httpClient;
        }

        public async Task<int> AddCompetitionDailyRecord(CompetitionDailyRecordCreateUpdateDto competitionDailyRecordCreateDto)
        {
            var entity = _mapper.Map<CompetitionDailyRecord>(competitionDailyRecordCreateDto);
            return await _competitionDailyRecordRepository.AddAsync(entity);
        }

        public Task<int> DeleteCompetitionDailyRecord(long id)
        {
            return _competitionDailyRecordRepository.DeleteAsync(id);
        }

        public async Task<(PagedResult<CompetitionDailyRecordWithDeletedDto>, int)> GetAllCompetitionDailyRecords(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await _competitionDailyRecordRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, true);
            var competitionDailyRecordWithDeletedDtos = _mapper.Map<PagedResult<CompetitionDailyRecordWithDeletedDto>>(result.Item1);
            return (competitionDailyRecordWithDeletedDtos, result.Item2);
        }

        public async Task<(PagedResult<CompetitionDailyRecordDto>, int)> GetAllExistingCompetitionDailyRecords(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await _competitionDailyRecordRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, false);
            var competitionDailyRecordDtos = _mapper.Map<PagedResult<CompetitionDailyRecordDto>>(result.Item1);
            return (competitionDailyRecordDtos, result.Item2);
        }

        public async Task<(CompetitionDailyRecordDto, int)> GetCompetitionDailyRecordById(long id)
        {
            var result = await _competitionDailyRecordRepository.GetByIdAsync(id);
            var competitionDailyRecordDto = _mapper.Map<CompetitionDailyRecordDto>(result.Item1);
            return (competitionDailyRecordDto, result.Item2);
        }

        public Task<int> UpdateCompetitionDailyRecord (long id, CompetitionDailyRecordCreateUpdateDto competitionDailyRecordUpdateDto)
        {
            var entity = _mapper.Map<CompetitionDailyRecord>(competitionDailyRecordUpdateDto);
            entity.id = id;
            return _competitionDailyRecordRepository.UpdateAsync(entity);
        }
    }
}