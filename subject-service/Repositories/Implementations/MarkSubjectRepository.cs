using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using subject_service.Common.Pagination;
using subject_service.DbMigrator;
using subject_service.Models;
using subject_service.Repositories.Interfaces;
using System.Linq.Expressions;

namespace subject_service.Repositories.Implementations
{
    public class MarkSubjectRepository : IMarkSubjectRepository
    {
        private readonly SubjectServiceDbContext _dbContext;

        public MarkSubjectRepository(SubjectServiceDbContext dbContext)
        {
            _dbContext = dbContext;
        }

        public async Task<int> AddAsync(MarkSubject entity)
        {
            try
            {
                var existing = await GetEntityExistAsync(entity);
                if (existing.Item2 == 1000)
                {
                    if (!existing.Item1.IsDeleted)
                    {
                        return 1002;
                    }

                    entity.Id = existing.Item1.Id;
                    _dbContext.markSubjects.Update(entity);
                }
                else if (existing.Item2 == 1001)
                {
                    return 1001;
                }
                else
                {
                    _dbContext.markSubjects.Add(entity);
                }
                await _dbContext.SaveChangesAsync();
                return 1000;
            }
            catch(Exception ex) 
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

                result.Item1.IsDeleted = true;
                _dbContext.markSubjects.Update(result.Item1);
                await _dbContext.SaveChangesAsync();
                return 1000;
            }
            catch
            {
                return 1001;
            }
        }

        public async Task<(PagedResult<MarkSubject>, int)> GetAllAsync(int page, int pageSize, string sortColumn, bool isDescending, bool getAll)
        {
            try
            {
                var query = _dbContext.markSubjects.IgnoreQueryFilters().AsNoTracking().Where(x => !x.IsDeleted || getAll);
                var totalCount = await query.CountAsync();

               if (!sortColumn.IsNullOrEmpty())
                {
                    var validColumns = typeof(MarkSubject)
                        .GetProperties()
                        .Select(p => p.Name)
                        .ToHashSet(StringComparer.OrdinalIgnoreCase);

                    if (!validColumns.Contains(sortColumn))
                    {
                        return (new PagedResult<MarkSubject>(), 1002);
                    }

                    query = isDescending
                        ? query.OrderByDescending(e => EF.Property<object>(e, sortColumn))
                        : query.OrderBy(e => EF.Property<object>(e, sortColumn));
                }

                var data = await query
                    .Skip((page - 1) * pageSize)
                         .Take(pageSize)
                         .ToListAsync();

                return (new PagedResult<MarkSubject>(data, totalCount, page, pageSize), 1000);
            }
            catch
            {
                return (new PagedResult<MarkSubject>(), 1001);
            }
        }

        public async Task<(MarkSubject, int)> GetByIdAsync(long id)
        {
            try
            {
                var entity = await _dbContext.markSubjects.AsNoTracking().FirstOrDefaultAsync(x => x.Id == id);
                return entity == null ? (new(), 1002) : (entity, 1000);
            }
            catch
            {
                return (new(), 1001);
            }
        }

        public async Task<(List<MarkSubject>, int)> GetByConditionAsync(Expression<Func<MarkSubject, bool>> expression)
        {
            try
            {
                var data = await _dbContext.markSubjects.AsNoTracking().Where(expression).ToListAsync();
                return data.Count == 0 ? (new(), 1002) : (data, 1000);
            }
            catch
            {
                return (new(), 1001);
            }
        }

        public async Task<(MarkSubject, int)> GetEntityExistAsync(MarkSubject entity)
        {
            try
            {
                var existing = await _dbContext.markSubjects.IgnoreQueryFilters().AsNoTracking().FirstOrDefaultAsync(s => s.MarkId == entity.MarkId && s.SubjectId == entity.SubjectId); 
                return existing == null ? (new(), 1002) : (existing, 1000);
            }
            catch
            {
                return (new(), 1001);
            }
        }

        public async Task<int> UpdateAsync(MarkSubject entity)
        {
            try
            {
                var existing = await GetByIdAsync(entity.Id);

                if (existing.Item2 > 1000)
                {
                    return existing.Item2;
                }

                _dbContext.markSubjects.Update(entity);
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