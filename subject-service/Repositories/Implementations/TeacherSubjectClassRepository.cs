using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using subject_service.Common.Pagination;
using subject_service.DbMigrator;
using subject_service.Models;
using subject_service.Repositories.Interfaces;
using System.Linq.Expressions;

namespace subject_service.Repositories.Implementations
{
    public class TeacherSubjectClassRepository : ITeacherSubjectClassRepository
    {
        private readonly SubjectServiceDbContext _dbContext;

        public TeacherSubjectClassRepository(SubjectServiceDbContext dbContext)
        {
            _dbContext = dbContext;
        }

        public async Task<int> AddAsync(TeacherSubjectClass entity)
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
                    _dbContext.teacherSubjectClasses.Update(entity);
                }
                else if (existing.Item2 == 1001)
                {
                    return 1001;
                }
                else
                {
                    _dbContext.teacherSubjectClasses.Add(entity);
                }
                await _dbContext.SaveChangesAsync();
                return 1000;
            }
            catch (Exception ex)
            {
                return 1001;
            }
        }

        public async Task<int> DeleteAsync(string teacherId, long subjectId, long classId)
        {
            try
            {
                var result = await GetByIdAsync(teacherId, subjectId, classId);
                if (result.Item2 > 1000)
                {
                    return result.Item2;
                }
                result.Item1.IsDeleted = true;
                _dbContext.teacherSubjectClasses.Update(result.Item1);
                await _dbContext.SaveChangesAsync();
                return 1000;
            }
            catch (Exception ex)
            {
                return 1001;
            }
        }

        public async Task<(PagedResult<TeacherSubjectClass>, int)> GetAllAsync(int page, int pageSize, string sortColumn, bool isDescending, bool getAll)
        {
            try
            {
                var query = _dbContext.teacherSubjectClasses.IgnoreQueryFilters().AsNoTracking().Where(s => !s.IsDeleted || getAll).AsQueryable();
                var totalCount = await query.CountAsync();

                if (!sortColumn.IsNullOrEmpty())
                {
                    var validColumns = typeof(TeacherSubjectClass)
                        .GetProperties()
                        .Select(p => p.Name)
                        .ToHashSet(StringComparer.OrdinalIgnoreCase);

                    if (!validColumns.Contains(sortColumn))
                    {
                        return (new PagedResult<TeacherSubjectClass>(), 1002);
                    }

                    query = isDescending
                        ? query.OrderByDescending(e => EF.Property<object>(e, sortColumn))
                        : query.OrderBy(e => EF.Property<object>(e, sortColumn));
                }

                var data = await query
                    .Skip((page - 1) * pageSize)
                    .Take(pageSize)
                    .ToListAsync();

                return (new PagedResult<TeacherSubjectClass>(data, totalCount, page, pageSize), 1000);
            }
            catch (Exception ex)
            {
                return (new PagedResult<TeacherSubjectClass>(), 1001);
            }
        }

        //public async Task<(IEnumerable<TeacherSubjectClass>, int)> GetAllAsync()
        //{
        //    try
        //    {
        //        var subjectTeacher = await _dbContext.teacherSubjectClasses.ToListAsync();
        //        return (subjectTeacher, 1000);
        //    }
        //    catch (Exception ex)
        //    {
        //        return (new List<TeacherSubjectClass>(), 1001);
        //    }
        //}

        public async Task<(TeacherSubjectClass, int)> GetEntityExistAsync(TeacherSubjectClass entity)
        {
            try
            {
                var existing = await _dbContext.teacherSubjectClasses.IgnoreQueryFilters().AsNoTracking().FirstOrDefaultAsync(s => s.SubjectId == entity.SubjectId && s.ClassId == entity.ClassId);
                return existing == null ? (new(), 1002) : (existing, 1000);
            }
            catch (Exception ex)
            {
                return (new(), 1001);
            }
        }

        public async Task<(List<TeacherSubjectClass>, int)> GetByConditionAsync(Expression<Func<TeacherSubjectClass, bool>> expression)
        {
            try
            {
                var data = await _dbContext.teacherSubjectClasses.AsNoTracking().Where(expression).ToListAsync();
                return data.Count == 0 ? (new(), 1002) : (data, 1000);
            }
            catch (Exception ex)
            {
                return (new(), 1001);
            }
        }

        public async Task<(TeacherSubjectClass, int)> GetByIdAsync(string teacherId, long subjectId, long classId)
        {
            try
            {
                var entity = await _dbContext.teacherSubjectClasses.AsNoTracking().FirstOrDefaultAsync(s => s.TeacherId == teacherId && s.SubjectId == subjectId && s.ClassId == classId);
                return entity == null ? (new(), 1002) : (entity, 1000);
            }
            catch (Exception ex)
            {
                return (new(), 1001);
            }
        }

        public async Task<int> UpdateAsync(TeacherSubjectClass entity)
        {
            try
            {
                var existing = await GetByIdAsync(entity.TeacherId, entity.SubjectId, entity.ClassId);
                if (existing.Item2 > 1000)
                {
                    return existing.Item2;
                }
                _dbContext.teacherSubjectClasses.Update(entity);
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
