namespace subject_service.Dtos.Mark
{
    public class MarkWithDeletedDto
    {
        public long Id { get; set; }

        //public decimal? ScoreAverage { get; set; }

        public int Weight { get; set; }

        public string? AcademicPerformance { get; set; }

        public string? Conduct { get; set; }

        public string? AwardTitle { get; set; }

        public bool? IsPassed { get; set; }

        public string StudentId { get; set; }

        public long ClassId { get; set; }

        public long? SemesterId { get; set; }

        public long SchoolYearId { get; set; }

        public bool IsDeleted { get; set; }
    }
}
