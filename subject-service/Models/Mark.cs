using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace subject_service.Models
{
    [Table("marks")]
    public partial class Mark
    {
        [Column("id")]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public long Id { get; set; }

        //[Column("score_average")]
        //public decimal? ScoreAverage { get; set; }

        [Column("weight")]
        public int Weight { get; set; }

        [Column("academic_performance")]
        public string? AcademicPerformance { get; set; }

        [Column("conduct")]
        public string? Conduct { get; set; }

        [Column("award_title")]
        public string? AwardTitle { get; set; }
        
        [Column("is_passed")]
        public bool? IsPassed { get; set; }

        [Column("student_id")]
        public string StudentId { get; set; }

        [Column("class_id")]
        public long ClassId { get; set; }

        [Column("semester_id")]
        public long? SemesterId { get; set; }

        [Column("school_year_id")]
        public long SchoolYearId { get; set; }

        [Column("is_deleted")]
        public bool IsDeleted { get; set; } = false;

        public virtual ICollection<MarkSubject> SubjectMarks { get; set; } = new List<MarkSubject>();
    }
}
