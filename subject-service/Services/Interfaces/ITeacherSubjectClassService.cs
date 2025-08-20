using subject_service.Common.Pagination;
using subject_service.Dtos.Teacher;
using subject_service.Dtos.TeacherSubjectClass;

namespace subject_service.Services.Interfaces
{
    public interface ITeacherSubjectClassService
    {
        //Task<(IEnumerable<TeacherSubjectClassDto>, int)> GetAllTeacherSubjectClasses();
        Task<(PagedResult<TeacherSubjectClassWithDeletedDto>, int)> GetAllTeacherSubjectClasses(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(PagedResult<TeacherSubjectClassDto>, int)> GetAllExistingTeacherSubjectClasses(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(List<TeacherDto>, int)> GetAllTeachersBySubjectId(long subjectId);
        Task<(List<long>, int)> GetAllSubjectIdsByTeacherId(string teacherId);
        //Task<(List<UserServiceTeacherDto>, int)> GetAllTeachersById(List<string> teacherIds);
        Task<(List<TeacherSubjectClassDetailDto>, int)> GetAllTeacherSubjectClassDetailByClassId(long classId);
        //Task<(List<string>, int)> GetAllIdsRoleTeacherById(List<string> teacherIds);
        Task<(TeacherSubjectClassDto, int)> GetTeacherSubjectClassById(string teacherId, long subjectId, long classId);
        Task<(bool, int)> CheckTeacherSubjectClassExists(string teacherId, long subjectId, long classId);
        Task<(List<string>, int)> AddTeacherSubjectClass(List<TeacherSubjectClassCreateUpdateDto> subjectTeacherCreateDtos);
        Task<int> UpdateTeacherSubjectClass(TeacherSubjectClassCreateUpdateDto subjectTeacherUpdateDto);
        Task<int> DeleteTeacherSubjectClass(string teacherId, long subjectId, long classId);
    }
}
