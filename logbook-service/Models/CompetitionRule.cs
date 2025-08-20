using System.ComponentModel.DataAnnotations.Schema;

namespace logbook_service.Models
{
    [Table("competition_rules")]
    public class CompetitionRule
    {
        [Column("id")]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public long id { get; set; }

        [Column("name")]
        public string name { get; set; }

        [Column("type")]
        public string type { get; set; }  //'reward' / 'violation'

        [Column("score")]
        public int score { get; set; }

        [Column("description")]
        public string description { get; set; }

        [Column("is_deleted")]
        public bool isDeleted { get; set; } = false;

        public virtual ICollection<CompetitionDailyDetail> CompetitionDailyDetails { get; set; } = new List<CompetitionDailyDetail>();
    }
}
