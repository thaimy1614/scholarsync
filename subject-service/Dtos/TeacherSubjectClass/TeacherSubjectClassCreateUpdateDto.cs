namespace subject_service.Dtos.TeacherSubjectClass
{
    public class TeacherSubjectClassCreateUpdateDto
    {
        public string TeacherId { get; set; }

        public long SubjectId { get; set; }

        public long ClassId { get; set; }
    }
}
