using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using logbook_service.DbMigrator;
using logbook_service.Models;
using logbook_service.Models.Dtos;
using logbook_service.Repositories.Interfaces;
using System.Linq.Expressions;

namespace logbook_service.Repositories.Implementations
{
    public class CompetitionDailyRecordRepository : ICompetitionDailyRecordRepository
    {
        private readonly LogbookServiceDbContext _dbContext;

        public CompetitionDailyRecordRepository(LogbookServiceDbContext dbContext)
        {
            _dbContext = dbContext;
        }

        public async Task<int> AddAsync(CompetitionDailyRecord entity)
        {
            try
            {
                var existing = await GetEntityExistAsync(entity);
                if (existing.Item2 == 1000)
                {
                    if (!existing.Item1.isDeleted)
                    {
                        return 1002;
                    }

                    entity.id = existing.Item1.id;
                    _dbContext.competitionDailyRecords.Update(entity);
                }
                else if (existing.Item2 == 1001)
                {
                    return 1001;
                }
                else
                {
                    _dbContext.competitionDailyRecords.Add(entity);
                }
                await _dbContext.SaveChangesAsync();
                return 1000;
            }
            catch
            {
                return 1001;
            }
        }

        public async Task<int> DeleteAsync(long id)
        {
            try
            {
                var result = await GetByIdAsync(id);
                if (result.Item2 > 1000)
                    return result.Item2;

                result.Item1.isDeleted = true;
                _dbContext.competitionDailyRecords.Update(result.Item1);
                await _dbContext.SaveChangesAsync();
                return 1000;
            }
            catch
            {
                return 1001;
            }
        }

        public async Task<(PagedResult<CompetitionDailyRecord>, int)> GetAllAsync(int page, int pageSize, string sortColumn, bool isDescending, bool getAll)
        {
            try
            {
                var query = _dbContext.competitionDailyRecords.Where(x => !x.isDeleted || getAll);
                var totalCount = await query.CountAsync();

               if (!sortColumn.IsNullOrEmpty())
                {
                    var validColumns = typeof(CompetitionDailyRecord)
                        .GetProperties()
                        .Select(p => p.Name)
                        .ToHashSet(StringComparer.OrdinalIgnoreCase);

                    if (!validColumns.Contains(sortColumn))
                    {
                        return (new PagedResult<CompetitionDailyRecord>(), 1002);
                    }

                    query = isDescending
                        ? query.OrderByDescending(e => EF.Property<object>(e, sortColumn))
                        : query.OrderBy(e => EF.Property<object>(e, sortColumn));
                }

                var data = await query
                    .Skip((page - 1) * pageSize)
                         .Take(pageSize)
                         .ToListAsync();

                return (new PagedResult<CompetitionDailyRecord>(data, totalCount, page, pageSize), 1000);
            }
            catch
            {
                return (new PagedResult<CompetitionDailyRecord>(), 1001);
            }
        }

        public async Task<(CompetitionDailyRecord, int)> GetByIdAsync(long id)
        {
            try
            {
                var entity = await _dbContext.competitionDailyRecords.FirstOrDefaultAsync(x => x.id == id);
                return entity == null ? (new(), 1002) : (entity, 1000);
            }
            catch
            {
                return (new(), 1001);
            }
        }

        public async Task<(List<CompetitionDailyRecord>, int)> GetByConditionAsync(Expression<Func<CompetitionDailyRecord, bool>> expression)
        {
            try
            {
                var data = await _dbContext.competitionDailyRecords.AsNoTracking().Where(expression).ToListAsync();
                return data.Count == 0 ? (new(), 1002) : (data, 1000);
            }
            catch
            {
                return (new(), 1001);
            }
        }

        public async Task<(CompetitionDailyRecord, int)> GetEntityExistAsync(CompetitionDailyRecord entity)
        {
            try
            {
                var existing = await _dbContext.competitionDailyRecords.AsNoTracking().FirstOrDefaultAsync(); // Add condition here if needed
                return existing == null ? (new(), 1002) : (existing, 1000);
            }
            catch
            {
                return (new(), 1001);
            }
        }

        public async Task<int> UpdateAsync(CompetitionDailyRecord entity)
        {
            try
            {
                var existing = await GetByIdAsync(entity.id);

                if (existing.Item2 > 1000)
                {
                    return existing.Item2;
                }

                _dbContext.competitionDailyRecords.Update(entity);
                await _dbContext.SaveChangesAsync();
                return 1000;
            }
            catch
            {
                return 1001;
            }
        }
    }
}