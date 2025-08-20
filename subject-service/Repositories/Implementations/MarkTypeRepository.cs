using Microsoft.AspNetCore.Mvc.RazorPages;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using subject_service.Common.Pagination;
using subject_service.DbMigrator;
using subject_service.Models;
using subject_service.Repositories.Interfaces;
using System.Linq.Expressions;

namespace subject_service.Repositories.Implementations
{
    public class MarkTypeRepository : IMarkTypeRepository
    {
        private readonly SubjectServiceDbContext _dbContext;

        public MarkTypeRepository(SubjectServiceDbContext dbContext)
        {
            _dbContext = dbContext;
        }

        public async Task<int> AddAsync(MarkType entity)
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
                    _dbContext.markTypes.Update(entity);
                }
                else if (existing.Item2 == 1001)
                {
                    return 1001;
                } else
                {
                    _dbContext.markTypes.Add(entity);
                }
                await _dbContext.SaveChangesAsync();
                return 1000;
            }
            catch (Exception ex)
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
                {
                    return result.Item2;
                }
                result.Item1.IsDeleted = true;
                _dbContext.markTypes.Update(result.Item1);
                await _dbContext.SaveChangesAsync();
                return 1000;
            }
            catch (Exception ex)
            {
                return 1001;
            }
        }

        public async Task<(PagedResult<MarkType>, int)> GetAllAsync(int page, int pageSize, string sortColumn, bool isDescending, bool getAll)
        {
            try
            {
                var query = _dbContext.markTypes.IgnoreQueryFilters().AsNoTracking().Where(s => !s.IsDeleted || getAll).AsQueryable();
                var totalCount = await query.CountAsync();

                if (!sortColumn.IsNullOrEmpty())
                {
                    var validColumns = typeof(MarkType)
                        .GetProperties()
                        .Select(p => p.Name)
                        .ToHashSet(StringComparer.OrdinalIgnoreCase);

                    if (!validColumns.Contains(sortColumn))
                    {
                        return (new PagedResult<MarkType>(), 1002);
                    }

                    query = isDescending
                        ? query.OrderByDescending(e => EF.Property<object>(e, sortColumn))
                        : query.OrderBy(e => EF.Property<object>(e, sortColumn));
                }

                var data = await query
                    .Skip((page - 1) * pageSize)
                .Take(pageSize)
                .ToListAsync();

                return (new PagedResult<MarkType>(data, totalCount, page, pageSize), 1000);
            }
            catch (Exception ex)
            {
                return (new PagedResult<MarkType>(), 1001);
            }
        }

        public async Task<(MarkType, int)> GetByIdAsync(long id)
        {
            try
            {
                var entity = await _dbContext.markTypes.AsNoTracking().FirstOrDefaultAsync(s => s.Id == id);
                return entity == null ? (new(), 1002) : (entity, 1000);
            }
            catch (Exception ex)
            {
                return (new(), 1001);
            }
        }

        public async Task<(List<MarkType>, int)> GetByConditionAsync(Expression<Func<MarkType, bool>> expression)
        {
            try
            {
                var data = await _dbContext.markTypes.AsNoTracking().Where(expression).ToListAsync();
                return data.Count == 0 ? (new(), 1002) : (data, 1000);
            }
            catch (Exception ex)
            {
                return (new(), 1001);
            }
        }

        public async Task<(MarkType, int)> GetEntityExistAsync(MarkType entity)
        {
            try
            {
                var existing = await _dbContext.markTypes.IgnoreQueryFilters().AsNoTracking().FirstOrDefaultAsync(s => s.Name == entity.Name && s.SubjectId == entity.SubjectId && s.ClassId == entity.ClassId);
                return existing == null ? (new(), 1002) : (existing, 1000);
            }
            catch (Exception ex)
            {
                return (new(), 1001);
            }
        }

        public async Task<int> UpdateAsync(MarkType entity)
        {
            try
            {
                var existing = await GetByIdAsync(entity.Id);
                if (existing.Item2 > 1000)
                {
                    return existing.Item2;
                }
                _dbContext.markTypes.Update(entity);
                await _dbContext.SaveChangesAsync();
                return 1000;
            }
            catch (Exception ex)
            {
                return 1001;
            }
        }
    }
}
