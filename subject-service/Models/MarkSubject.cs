using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace subject_service.Models
{
    [Table("mark_subject")]
    public partial class MarkSubject
    {
        [Column("id")]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public long Id { get; set; }

        [Column("score_average")]
        public decimal? ScoreAverage { get; set; }

        [Column("qualitative_score_average")]
        public string? QualitativeScoreAverage { get; set; }

        [Column("mark_id")]
        public long MarkId { get; set; }

        [Column("subject_id")]
        public long SubjectId { get; set; }
        
        [Column("note")]
        public string Note { get; set; }

        [Column("is_deleted")]
        public bool IsDeleted { get; set; } = false;

        [JsonIgnore]
        public virtual Mark? Mark { get; set; }

        [JsonIgnore]
        public virtual Subject? Subject { get; set; }

        public virtual ICollection<ComponentPoint> ComponentPoints { get; set; } = new List<ComponentPoint>();
    }
}
