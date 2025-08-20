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
    public class MarkRepository : IMarkRepository
    {
        private readonly SubjectServiceDbContext _dbContext;

        public MarkRepository(SubjectServiceDbContext dbContext)
        {
            _dbContext = dbContext;
        }

        public async Task<int> AddAsync(Mark entity)
        {
            try
            {
                /*if (entity.Conduct is not null &&
                    !Enum.IsDefined(typeof(ConductType), entity.Conduct))
                {
                    return 1003; // Invalid Conduct enum
                }

                if (entity.AcademicPerformance is not null &&
                    !Enum.IsDefined(typeof(AcademicPerformanceType), entity.AcademicPerformance))
                {
                    return 1004; // Invalid AcademicPerformance enum
                }*/

                var existing = await GetEntityExistAsync(entity);
                if (existing.Item2 == 1000)
                {
                    if (!existing.Item1.IsDeleted)
                    {
                        return 1002;
                    }
                    entity.Id = existing.Item1.Id;
                    _dbContext.marks.Update(entity);
                }
                else if (existing.Item2 == 1001)
                {
                    return 1001;
                }
                else
                {
                    _dbContext.marks.Add(entity);
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
                _dbContext.marks.Update(result.Item1);
                await _dbContext.SaveChangesAsync();
                return 1000;
            }
            catch (Exception ex)
            {
                return 1001;
            }
        }

        public async Task<(PagedResult<Mark>, int)> GetAllAsync(int page, int pageSize, string sortColumn, bool isDescending, bool getAll)
        {
            try
            {
                var query = _dbContext.marks.IgnoreQueryFilters().AsNoTracking().Where(s => !s.IsDeleted || getAll).AsQueryable();
                var totalCount = await query.CountAsync();

                if (!sortColumn.IsNullOrEmpty())
                {
                    var validColumns = typeof(Mark)
                        .GetProperties()
                        .Select(p => p.Name)
                        .ToHashSet(StringComparer.OrdinalIgnoreCase); 

                    if (!validColumns.Contains(sortColumn))
                    {
                        return (new PagedResult<Mark>(), 1002);
                    }

                    query = isDescending
                        ? query.OrderByDescending(e => EF.Property<object>(e, sortColumn))
                        : query.OrderBy(e => EF.Property<object>(e, sortColumn));
                }

                var data = await query
                    .Skip((page - 1) * pageSize)
                .Take(pageSize)
                .ToListAsync();

                return (new PagedResult<Mark>(data, totalCount, page, pageSize), 1000);
            }
            catch (Exception ex)
            {
                return (new PagedResult<Mark>(), 1001);
            }
        }

        public async Task<(List<Mark>, int)> GetByConditionAsync(Expression<Func<Mark, bool>> expression)
        {
            try
            {
                var data = await _dbContext.marks.AsNoTracking().Where(expression).ToListAsync();
                return data.Count == 0 ? (new(), 1002) : (data, 1000);
            }
            catch (Exception ex)
            {
                return (new(), 1001);
            }
        }

        public async Task<(Mark, int)> GetByIdAsync(long id)
        {
            try
            {
                var entity = await _dbContext.marks.AsNoTracking().FirstOrDefaultAsync(s => s.Id == id && !s.IsDeleted);
                return entity == null ? (new(), 1002) : (entity, 1000);
            }
            catch (Exception ex)
            {
                return (new(), 1001);
            }
        }

        public async Task<(Mark, int)> GetEntityExistAsync(Mark entity)
        {
            try
            {
                var mark = await _dbContext.marks.IgnoreQueryFilters().AsNoTracking().FirstOrDefaultAsync(s =>  s.StudentId == entity.StudentId && s.SchoolYearId == entity.SchoolYearId && s.SemesterId == entity.SemesterId && s.ClassId == entity.ClassId);
                if (mark == null)
                {
                    return (new(), 1002);
                }
                return (mark, 1000);
            }
            catch (Exception ex)
            {
                return (new(), 1001);
            }
        }

        public async Task<int> UpdateAsync(Mark entity)
        {
            try
            {
                var existing = await GetByIdAsync(entity.Id);
                if (existing.Item2 > 1000)
                {
                    return existing.Item2;
                }
                _dbContext.marks.Update(entity);
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
