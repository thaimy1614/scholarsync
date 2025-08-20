using Microsoft.AspNetCore.Mvc.RazorPages;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using subject_service.Common.Pagination;
using subject_service.DbMigrator;
using subject_service.Models;
using subject_service.Repositories.Interfaces;
using System;
using System.Linq.Expressions;

namespace subject_service.Repositories.Implementations
{
    public class SubjectRepository : ISubjectRepository
    {
        private readonly SubjectServiceDbContext _dbContext;

        public SubjectRepository(SubjectServiceDbContext dbContext)
        {
            _dbContext = dbContext;
        }

        public async Task<int> AddAsync(Subject entity)
        {
            try
            {
                var existing = await GetEntityExistAsync(entity);
                if (existing.Item2 == 1000)
                {
                    if(!existing.Item1.IsDeleted)
                    {
                        return 1002;
                    }
                    entity.Id = existing.Item1.Id;
                    _dbContext.subjects.Update(entity);
                } else if(existing.Item2 == 1001)
                {
                    return 1001;
                } else
                {
                    _dbContext.subjects.Add(entity);
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
                if (result.Item2 > 1000) {
                    return result.Item2;
                }
                result.Item1.IsDeleted = true;
                _dbContext.subjects.Update(result.Item1);
                await _dbContext.SaveChangesAsync();
                return 1000;
            }
            catch (Exception ex)
            {
                return 1001;
            }
        }

        public async Task<(PagedResult<Subject>, int)> GetAllAsync(int page, int pageSize, string sortColumn, bool isDescending, bool getAll)
        {
            try
            {
                var query = _dbContext.subjects.IgnoreQueryFilters().AsNoTracking().Where(s => !s.IsDeleted || getAll).AsQueryable();
                var totalCount = await query.CountAsync();

                if (!sortColumn.IsNullOrEmpty())
                {
                    var validColumns = typeof(Subject)
                        .GetProperties()
                        .Select(p => p.Name)
                        .ToHashSet(StringComparer.OrdinalIgnoreCase);

                    if (!validColumns.Contains(sortColumn))
                    {
                        return (new PagedResult<Subject>(), 1002);
                    }

                    query = isDescending
                        ? query.OrderByDescending(e => EF.Property<object>(e, sortColumn))
                        : query.OrderBy(e => EF.Property<object>(e, sortColumn));
                }

                var data = await query
                    .Skip((page - 1) * pageSize)
                    .Take(pageSize)
                    .ToListAsync();

                return (new PagedResult<Subject>(data, totalCount, page, pageSize), 1000);
            }
            catch (Exception ex)
            {
                return (new PagedResult<Subject>(), 1001);
            }
        }

        public async Task<(Subject, int)> GetByIdAsync(long id)
        {
            try
            {
                var entity = await _dbContext.subjects.AsNoTracking().FirstOrDefaultAsync(s => s.Id == id);
                return entity == null ? (new(), 1002) : (entity, 1000);
            }
            catch (Exception ex)
            {
                return (new(), 1001);
            }
        }

        public async Task<(List<Subject>, int)> GetByConditionAsync(Expression<Func<Subject, bool>> expression)
        {
            try
            {
                var data = await _dbContext.subjects.AsNoTracking().Where(expression).ToListAsync();
                return data.Count == 0 ? (new(), 1002) : (data, 1000);
            }
            catch (Exception ex)
            {
                return (new(), 1001);
            }
        }
        
        public async Task<(PagedResult<Subject>, int)> SearchAsync(string? keyword, int page, int pageSize, string sortColumn, bool isDescending, bool getAll)
        {
            try
            {
                var query = _dbContext.subjects.IgnoreQueryFilters().AsNoTracking().Where(s => !s.IsDeleted || getAll).AsQueryable();
                var totalCount = await query.CountAsync();

                if (!sortColumn.IsNullOrEmpty())
                {
                    var validColumns = typeof(Subject)
                        .GetProperties()
                        .Select(p => p.Name)
                        .ToHashSet(StringComparer.OrdinalIgnoreCase);

                    if (!validColumns.Contains(sortColumn))
                    {
                        return (new PagedResult<Subject>(), 1002);
                    }

                    query = isDescending
                        ? query.OrderByDescending(e => EF.Property<object>(e, sortColumn))
                        : query.OrderBy(e => EF.Property<object>(e, sortColumn));
                }


                if (!string.IsNullOrEmpty(keyword))
                {
                    query = query.Where(s =>
                        s.Name.ToLower().Contains(keyword) ||
                        //s.weeklySlots.ToString().Contains(keyword) ||
                        //s.maxSlotsPerSession.ToString().Contains(keyword) ||
                        //s.preferConsecutive.ToString().ToLower().Contains(keyword) ||
                        //s.isMainSubject.ToString().ToLower().Contains(keyword) ||
                        //s.isScoreable.ToString().ToLower().Contains(keyword) ||
                        //s.isEvaluateWithScore.ToString().ToLower().Contains(keyword) ||
                        //(s.specialSlot != null && s.specialSlot.ToString().Contains(keyword)) ||
                        //(s.specialRoom != null && s.specialRoom.ToString().Contains(keyword)) ||
                        s.SchoolYearId.ToString().Contains(keyword)
                    );
                }

                var data = await query
                    .Skip((page - 1) * pageSize)
                    .Take(pageSize)
                    .ToListAsync();

                return (new PagedResult<Subject>(data, totalCount, page, pageSize), 1000);
            }
            catch (Exception ex)
            {
                return (new(), 1001);
            }
        }

        public async Task<(Subject, int)> GetEntityExistAsync(Subject entity)
        {
            try
            {
                var existing = await _dbContext.subjects.IgnoreQueryFilters().AsNoTracking().FirstOrDefaultAsync(s => s.Name == entity.Name && s.SchoolYearId == entity.SchoolYearId);
                return existing == null ? (new(), 1002) : (existing, 1000);
            }
            catch (Exception ex)
            {
                return (new(), 1001);
            }
        }

        public async Task<int> UpdateAsync(Subject entity)
        {
            try
            {
                var existing = await GetByIdAsync(entity.Id);
                if(existing.Item2 > 1000)
                {
                    return existing.Item2;
                }
                _dbContext.subjects.Update(entity);
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
