using System.ComponentModel.DataAnnotations.Schema;

namespace logbook_service.Models
{
    [Table("competition_period_scores")]
    public class CompetitionPeriodScore
    {
        [Column("id")]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public long id { get; set; }

        [Column("class_id")]
        public long classId { get; set; }

        [Column("teacher_id")]
        public string teacherId { get; set; }

        [Column("record_date")]
        public DateTime recordDate { get; set; }

        [Column("period_number")]
        public int periodNumber { get; set; }

        [Column("score_grade")]
        public string scoreGrade { get; set; } // A+, A, B+,...

        [Column("score_value")]
        public int scoreValue { get; set; }

        [Column("note")]
        public string note { get; set; }

        [Column("is_deleted")]
        public bool isDeleted { get; set; } = false;
    }
}
