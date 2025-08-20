using subject_service.Dtos.Class;
using subject_service.Dtos.Student;

namespace subject_service.Services.Interfaces
{
    public interface ISchoolClientService
    {
        Task<(ClassDto, int)> GetClassNameByClassId(long classId);
        Task<(List<StudentDto>, int)> GetListStudentByClassId(long classId);
    }
}
