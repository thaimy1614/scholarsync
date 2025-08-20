using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace logbook_service.Models
{
    [Table("competition_daily_details")]
    public class CompetitionDailyDetail
    {
        [Column("id")]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public long id { get; set; }

        [Column("competition_daily_record_id")]
        public long competitionDailyRecordId { get; set; }

        [Column("competition_rule_id")]
        public long competitionRuleId { get; set; }

        [Column("student_id")]
        public string? studentId { get; set; } // nullable for all class

        [Column("score")]
        public int score { get; set; }

        [Column("note")]
        public string note { get; set; }

        [Column("is_deleted")]
        public bool isDeleted { get; set; } = false;

        [JsonIgnore]
        public virtual CompetitionDailyRecord? CompetitionDailyRecord { get; set; }
        
        [JsonIgnore]
        public virtual CompetitionRule? CompetitionRule { get; set; }
    }
}
