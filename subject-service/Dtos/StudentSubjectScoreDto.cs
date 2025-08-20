namespace subject_service.Dtos
{
    public class StudentSubjectScoreDto
    {
        public long id { get; set; }
        public string student_id { get; set; }
        public string full_name { get; set; }
        public string image { get; set; }
        public long subject_id { get; set; }
        public string subject_name { get; set; }
        public long class_id { get; set; }
        public long mark_id { get; set; }
        public long semester_id { get; set; }
        public string academic_performance { get; set; }
        public float score_average { get; set; }

        public virtual ICollection<StudentScoreDto> StudentScores { get; set; } = new List<StudentScoreDto>();
    }
}
