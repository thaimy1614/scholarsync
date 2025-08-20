using subject_service.Dtos.Student;
using subject_service.Dtos.Teacher;

namespace subject_service.Services.Interfaces
{
    public interface IUserClientService
    {
        Task<(List<string>, int)> GetAllIdsRoleTeacherById(List<string> teacherIds);
        Task<(List<TeacherDto>, int)> GetAllTeachersById(List<string> teacherIds);
        Task<(List<string>, int)> GetAllIdsRoleStudentById(List<string> studentIds);
        Task<(List<StudentDto>, int)> GetAllStudentsById(List<string> studentIds);
    }
}
