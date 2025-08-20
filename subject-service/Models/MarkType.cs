using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace subject_service.Models
{
    [Table("mark_types")]
    public partial class MarkType
    {
        [Column("id")]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public long Id { get; set; }

        [Column("name")]
        public string Name { get; set; }

        [Column("weight")]
        public int Weight { get; set; }

        [Column("total_column")]
        public int TotalColumn { get; set; }

        [Column("subject_id")]
        public long SubjectId { get; set; }
        
        [Column("class_id")]
        public long ClassId { get; set; }

        [Column("is_deleted")]
        public bool IsDeleted { get; set; } = false;

        [JsonIgnore]
        public virtual Subject? Subject { get; set; }

        public virtual ICollection<ComponentPoint> ComponentPoints { get; set; } = new List<ComponentPoint>();
    }
}
