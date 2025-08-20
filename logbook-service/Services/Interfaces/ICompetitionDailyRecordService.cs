using logbook_service.Models;
using logbook_service.Models.Dtos;

namespace logbook_service.Services.Interfaces
{
    public interface ICompetitionDailyRecordService
    {
        Task<(PagedResult<CompetitionDailyRecordWithDeletedDto>, int)> GetAllCompetitionDailyRecords(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(PagedResult<CompetitionDailyRecordDto>, int)> GetAllExistingCompetitionDailyRecords(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(CompetitionDailyRecordDto, int)> GetCompetitionDailyRecordById(long id);
        Task<int> AddCompetitionDailyRecord(CompetitionDailyRecordCreateUpdateDto CompetitionDailyRecordCreateDto);
        Task<int> UpdateCompetitionDailyRecord(long id, CompetitionDailyRecordCreateUpdateDto CompetitionDailyRecordUpdateDto);
        Task<int> DeleteCompetitionDailyRecord(long id);
    }
}