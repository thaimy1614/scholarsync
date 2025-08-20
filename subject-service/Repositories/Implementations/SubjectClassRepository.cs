using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using subject_service.Common.Pagination;
using subject_service.DbMigrator;
using subject_service.Models;
using subject_service.Repositories.Interfaces;
using System.Linq.Expressions;

namespace subject_service.Repositories.Implementations
{
    public class SubjectClassRepository : ISubjectClassRepository
    {
        private readonly SubjectServiceDbContext _dbContext;

        public SubjectClassRepository(SubjectServiceDbContext dbContext)
        {
            _dbContext = dbContext;
        }

        public async Task<int> AddAsync(SubjectClass entity)
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

                    //entity.id = existing.Item1.id;
                    _dbContext.subjectClasses.Update(entity);
                }
                else if (existing.Item2 == 1001)
                {
                    return 1001;
                }
                else
                {
                    _dbContext.subjectClasses.Add(entity);
                }
                await _dbContext.SaveChangesAsync();
                return 1000;
            }
            catch(Exception ex)
            {
                return 1001;
            }
        }

        public async Task<int> DeleteAsync(long subjectId, long classId)
        {
            try
            {
                var result = await GetByIdAsync(subjectId, classId);
                if (result.Item2 > 1000)
                    return result.Item2;

                result.Item1.IsDeleted = true;
                _dbContext.subjectClasses.Update(result.Item1);
                await _dbContext.SaveChangesAsync();
                return 1000;
            }
            catch
            {
                return 1001;
            }
        }

        public async Task<(PagedResult<SubjectClass>, int)> GetAllAsync(int page, int pageSize, string sortColumn, bool isDescending, bool getAll)
        {
            try
            {
                var query = _dbContext.subjectClasses.IgnoreQueryFilters().AsNoTracking().Where(s => !s.IsDeleted || getAll);
                var totalCount = await query.CountAsync();

               if (!sortColumn.IsNullOrEmpty())
                {
                    var validColumns = typeof(SubjectClass)
                        .GetProperties()
                        .Select(p => p.Name)
                        .ToHashSet(StringComparer.OrdinalIgnoreCase);

                    if (!validColumns.Contains(sortColumn))
                    {
                        return (new PagedResult<SubjectClass>(), 1002);
                    }

                    query = isDescending
                        ? query.OrderByDescending(e => EF.Property<object>(e, sortColumn))
                        : query.OrderBy(e => EF.Property<object>(e, sortColumn));
                }

                var data = await query
                    .Skip((page - 1) * pageSize)
                         .Take(pageSize)
                         .ToListAsync();

                return (new PagedResult<SubjectClass>(data, totalCount, page, pageSize), 1000);
            }
            catch
            {
                return (new PagedResult<SubjectClass>(), 1001);
            }
        }

        public async Task<(SubjectClass, int)> GetByIdAsync(long subjectId, long classId)
        {
            try
            {
                var entity = await _dbContext.subjectClasses.AsNoTracking().FirstOrDefaultAsync(s => s.SubjectId == subjectId && s.ClassId == classId);
                return entity == null ? (new(), 1002) : (entity, 1000);
            }
            catch
            {
                return (new(), 1001);
            }
        }

        public async Task<(List<SubjectClass>, int)> GetByConditionAsync(Expression<Func<SubjectClass, bool>> expression)
        {
            try
            {
                var data = await _dbContext.subjectClasses.AsNoTracking().Where(expression).ToListAsync();
                return data.Count == 0 ? (new(), 1002) : (data, 1000);
            }
            catch
            {
                return (new(), 1001);
            }
        }

        public async Task<(SubjectClass, int)> GetEntityExistAsync(SubjectClass entity)
        {
            try
            {
                var existing = await _dbContext.subjectClasses.IgnoreQueryFilters().AsNoTracking().FirstOrDefaultAsync(s => s.SubjectId == entity.SubjectId && s.ClassId == entity.ClassId);
                return existing == null ? (new(), 1002) : (existing, 1000);
            }
            catch
            {
                return (new(), 1001);
            }
        }

        public async Task<int> UpdateAsync(SubjectClass entity)
        {
            try
            {
                var existing = await GetByIdAsync(entity.SubjectId, entity.ClassId);

                if (existing.Item2 > 1000)
                {
                    return existing.Item2;
                }

                _dbContext.subjectClasses.Update(entity);
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