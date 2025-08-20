using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using subject_service.Common.Pagination;
using subject_service.DbMigrator;
using subject_service.Models;
using subject_service.Repositories.Interfaces;
using System.Linq.Expressions;

namespace subject_service.Repositories.Implementations
{
    public class ComponentPointRepository : IComponentPointRepository
    {
        private readonly SubjectServiceDbContext _dbContext;

        public ComponentPointRepository(SubjectServiceDbContext dbContext)
        {
            _dbContext = dbContext;
        }

        public async Task<int> AddAsync(ComponentPoint entity)
        {
            try
            {
                var item = await GetEntityExistAsync(entity);
                if (item.Item2 == 1000)
                {
                    if (!item.Item1.IsDeleted)
                    {
                        return 1002;
                    }
                    entity.Id = item.Item1.Id;
                    _dbContext.componentPoints.Update(entity);
                }
                else if (item.Item2 == 1001)
                {
                    return 1001;
                } else
                {
                    _dbContext.componentPoints.Add(entity);
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
                var item = await GetByIdAsync(id);
                if (item.Item2 > 1000)
                {
                    return item.Item2;
                }
                item.Item1.IsDeleted = true;
                _dbContext.componentPoints.Update(item.Item1);
                await _dbContext.SaveChangesAsync();
                return 1000;
            }
            catch (Exception ex)
            {
                return 1001;
            }
        }

        public async Task<(PagedResult<ComponentPoint>, int)> GetAllAsync(
            int page, int pageSize, string sortColumn, bool isDescending, bool getAll)
        {
            try
            {
                var query = _dbContext.componentPoints.IgnoreQueryFilters().AsNoTracking().Where(s => !s.IsDeleted || getAll).AsQueryable();

                var totalCount = await query.CountAsync();


                if (!sortColumn.IsNullOrEmpty())
                {
                    var validColumns = typeof(ComponentPoint)
                        .GetProperties()
                        .Select(p => p.Name)
                        .ToHashSet(StringComparer.OrdinalIgnoreCase);

                    if (!validColumns.Contains(sortColumn))
                    {
                        return (new PagedResult<ComponentPoint>(), 1002);
                    }

                    query = isDescending
                        ? query.OrderByDescending(e => EF.Property<object>(e, sortColumn))
                        : query.OrderBy(e => EF.Property<object>(e, sortColumn));
                }

                var data = await query
                    .Skip((page - 1) * pageSize)
                    .Take(pageSize)
                    .ToListAsync();

                return (new PagedResult<ComponentPoint>(data, totalCount, page, pageSize), 1000);
            }
            catch (Exception ex)
            {
                return (new PagedResult<ComponentPoint>(), 1001);
            }
        }

        public async Task<(ComponentPoint, int)> GetByIdAsync(long id)
        {
            try
            {
                var entity = await _dbContext.componentPoints.AsNoTracking().FirstOrDefaultAsync(s => s.Id == id);
                return entity == null ? (new(), 1002) : (entity, 1000);
            }
            catch (Exception ex)
            {
                return (new(), 1001);
            }
        }

        public async Task<(List<ComponentPoint>, int)> GetByConditionAsync(Expression<Func<ComponentPoint, bool>> expression)
        {
            try
            {
                var data = await _dbContext.componentPoints.AsNoTracking().Where(expression).ToListAsync();
                return data.Count == 0 ? (new(), 1002) : (data, 1000);
            }
            catch (Exception ex)
            {
                return (new(), 1001);
            }
        }

        public async Task<(ComponentPoint, int)> GetEntityExistAsync(ComponentPoint entity)
        {
            try
            {
                var existing = await _dbContext.componentPoints.IgnoreQueryFilters().AsNoTracking().FirstOrDefaultAsync(s => s.ColumnOrder == entity.ColumnOrder && s.MarkSubjectId == entity.MarkSubjectId && s.MarkTypeId == entity.MarkTypeId);
                return existing == null ? (new(), 1002) : (existing, 1000);
            }
            catch (Exception ex)
            {
                return (new(), 1001);
            }
        }

        public async Task<int> UpdateAsync(ComponentPoint entity)
        {
            try
            {
                var existing = await GetByIdAsync(entity.Id);
                if (existing.Item2 > 1000)
                {
                    return 1001;
                }
                _dbContext.componentPoints.Update(entity);
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
