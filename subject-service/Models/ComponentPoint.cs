using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace subject_service.Models
{
    [Table("component_points")]
    public partial class ComponentPoint
    {
        [Column("id")]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public long Id { get; set; }

        [Column("score")]
        public decimal? Score { get; set; }
        
        [Column("qualitative_score")]
        public string? QualitativeScore { get; set; }

        [Column("column_order")]
        public int ColumnOrder { get; set; }

        [Column("mark_subject_id")]
        public long MarkSubjectId { get; set; }

        [Column("mark_type_id")]
        public long MarkTypeId { get; set; }

        [Column("is_pass_fail_type")]
        public bool IsPassFailType { get; set; }

        [Column("is_deleted")]
        public bool IsDeleted { get; set; } = false;

        [JsonIgnore]
        public virtual MarkType? MarkType { get; set; }

        [JsonIgnore]
        public virtual MarkSubject? MarkSubject { get; set; }
    }
}
