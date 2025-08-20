namespace subject_service.Dtos.MarkSubject
{
    public class MarkSubjectDto
    {
        public decimal? ScoreAverage { get; set; }

        public string? QualitativeScoreAverage { get; set; }

        public long MarkId { get; set; }

        public long SubjectId { get; set; }

        public string Note { get; set; }
    }
}