using System.ComponentModel.DataAnnotations.Schema;

namespace subject_service.Models
{
    [Table("teacher_subject_classes")]
    public class TeacherSubjectClass
    {
        [Column("teacher_id")]
        public string TeacherId { get; set; }

        [Column("subject_id")]
        public long SubjectId { get; set; }

        [Column("class_id")]
        public long ClassId { get; set; }

        [Column("is_deleted")]
        public bool IsDeleted { get; set; } = false;

        public virtual Subject? Subject { get; set; }
    }
}
