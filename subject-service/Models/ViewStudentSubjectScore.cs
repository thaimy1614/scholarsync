using subject_service.Dtos;
using System.ComponentModel.DataAnnotations.Schema;

namespace subject_service.Models
{
    public class ViewStudentSubjectScore
    {
        [Column("id")]
        public long Id { get; set; }

        [Column("student_id")]
        public string StudentId { get; set; }

        [Column("subject_id")]
        public long SubjectId { get; set; }

        [Column("subject_name")]
        public string SubjectName { get; set; }

        [Column("class_id")]
        public long ClassId { get; set; }

        [Column("semester_id")]
        public long? SemesterId { get; set; }

        [Column("school_year_id")]
        public long SchoolYearId { get; set; }
        
        [Column("mark_subject_id")]
        public long MarkSubjectId { get; set; }

        [Column("component_type")]
        public string ComponentType { get; set; }

        [Column("column_order")]
        public int ColumnOrder { get; set; }

        [Column("qualitative_score")]
        public string? QualitativeScore { get; set; }

        [Column("is_evaluate_with_score")]
        public bool IsEvaluateWithScore { get; set; }
        
        [Column("score")]
        public decimal? Score { get; set; }
        
        [Column("subject_score_average")]
        public decimal? SubjectScoreAverage { get; set; }
        
        [Column("subject_qualitative_score_average")]
        public string? SubjectQualitativeScoreAverage { get; set; }

        [Column("award_title")]
        public string? AwardTitle { get; set; }

        [Column("is_passed")]
        public bool? IsPassed { get; set; }

        //[Column("total_average")]
        //public decimal? TotalAverage { get; set; }

        [Column("academic_performance")]
        public string? AcademicPerformance {  get; set; }

        [Column("conduct")]
        public string? Conduct {  get; set; }

        [Column("note")]
        public string? Note { get; set; }
    }
}
