using System.ComponentModel.DataAnnotations.Schema;

namespace subject_service.Dtos.StudentSubjectScore
{
    public class ViewStudentSubjectScoreDto
    {
        public long Id { get; set; }

        public string StudentId { get; set; }

        public long SubjectId { get; set; }

        public string SubjectName { get; set; }

        public long ClassId { get; set; }

        public long? SemesterId { get; set; }

        public long SchoolYearId { get; set; }

        public long MarkSubjectId { get; set; }

        public string ComponentType { get; set; }

        public int ColumnOrder { get; set; }

        public string? QualitativeScore { get; set; }

        public bool IsEvaluateWithScore { get; set; }

        public decimal? Score { get; set; }

        public decimal? SubjectScoreAverage { get; set; }

        public string? SubjectQualitativeScoreAverage { get; set; }

        public string? AwardTitle { get; set; }

        public bool? IsPassed { get; set; }

        public string? AcademicPerformance { get; set; }

        public string? Conduct { get; set; }

        public string? Note { get; set; }
    }
}
