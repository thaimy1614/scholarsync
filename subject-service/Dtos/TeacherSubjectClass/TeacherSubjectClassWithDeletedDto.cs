namespace subject_service.Dtos.TeacherSubjectClass
{
    public class TeacherSubjectClassWithDeletedDto
    {
        public string TeacherId { get; set; }

        public long SubjectId { get; set; }

        public long ClassId { get; set; }

        public bool IsDeleted { get; set; }
    }
}
