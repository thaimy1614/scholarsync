namespace subject_service.Dtos.ComponentPoint
{
    public class ComponentPointDto
    {
        public long Id { get; set; }

        public decimal? Score { get; set; }

        public string? QualitativeScore { get; set; }

        public int ColumnOrder { get; set; }

        public long MarkSubjectId { get; set; }

        public long MarkTypeId { get; set; }

        public bool IsPassFailType { get; set; }
    }
}
