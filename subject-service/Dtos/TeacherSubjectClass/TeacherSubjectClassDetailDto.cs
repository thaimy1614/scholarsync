using subject_service.Dtos.Teacher;

namespace subject_service.Dtos.TeacherSubjectClass
{
    public class TeacherSubjectClassDetailDto
    {
        public string TeacherId { get; set; } = null!;

        public string FullName { get; set; } = null!;

        public string Image { get; set; } = null!;

        public string Email { get; set; } = null!;

        public string PhoneNumber { get; set; } = null!;

        public string Address { get; set; } = null!;

        public string Gender { get; set; } = null!;

        public long SubjectId { get; set; }

        public string SubjectName { get; set; } = null!;

        public int WeeklySlots { get; set; }

        public int MaxSlotsPerSession { get; set; }

        public bool PreferConsecutive { get; set; }

        public bool IsMainSubject { get; set; }

        public bool IsScoreable { get; set; }

        public bool IsEvaluateWithScore { get; set; }

        public int? SpecialSlot { get; set; }

        public long? SpecialRoom { get; set; }

        public long SchoolYearId { get; set; }

        public long ClassId { get; set; }

        public string ClassName { get; set; } = null!;

        public TeacherClassDto? HomeroomTeacher { get; set; }
    }
}
