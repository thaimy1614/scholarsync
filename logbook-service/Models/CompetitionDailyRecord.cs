using System.ComponentModel.DataAnnotations.Schema;

namespace logbook_service.Models
{
    [Table("competition_daily_records")]
    public class CompetitionDailyRecord
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

        [Column("total_score")]
        public int totalScore { get; set; }

        [Column("note")]
        public string note { get; set; }

        [Column("is_deleted")]
        public bool isDeleted { get; set; } = false;

        public virtual ICollection<CompetitionDailyDetail> CompetitionDailyDetails { get; set; } = new List<CompetitionDailyDetail>();
    }
}
