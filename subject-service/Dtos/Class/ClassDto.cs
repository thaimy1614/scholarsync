using subject_service.Dtos.Teacher;

namespace subject_service.Dtos.Class
{
    public class ClassDto
    {
        public long ClassId { get; set; }
        public string ClassName { get; set; }

        public TeacherClassDto? Teacher { get; set; }
    }
}
