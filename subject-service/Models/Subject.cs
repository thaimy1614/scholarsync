using subject_service.Models;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace subject_service.Models
{
    [Table("subjects")]
    public partial class Subject
    {
        [Column("id")]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public long Id { get; set; }

        [Column("name")]
        public string Name { get; set; }

        //so tiet trong tuan
        [Column("weekly_slots")]
        public int WeeklySlots { get; set; }

        //so tiet trong buoi
        [Column("max_slots_per_session")]
        public int MaxSlotsPerSession { get; set; }

        //tiet doi
        [Column("prefer_consecutive")]
        public bool PreferConsecutive { get; set; }

        //mon hoc chinh
        [Column("is_main_subject")]
        public bool IsMainSubject { get; set; }

        //tiet dac biet
        [Column("special_slot")]
        public int? SpecialSlot { get; set; }

        //phong dac biet
        [Column("special_room")]
        public long? SpecialRoom { get; set; }

        //co tinh diem
        [Column("is_scoreable")]
        public bool IsScoreable { get; set; }

        //danh gia bang diem (D hay KD)
        [Column("is_evaluate_with_score")]
        public bool IsEvaluateWithScore {  get; set; }

        [Column("school_year_id")]
        public long SchoolYearId { get; set; }

        [Column("is_deleted")]
        public bool IsDeleted { get; set; } = false;

        public virtual ICollection<MarkSubject> SubjectMarks { get; set; } = new List<MarkSubject>();

        public virtual ICollection<MarkType> MarkTypes { get; set; } = new List<MarkType>();

        public virtual ICollection<TeacherSubjectClass> TeacherSubjectClasses { get; set; } = new List<TeacherSubjectClass>();

        public virtual ICollection<SubjectClass> SubjectClasses { get; set; } = new List<SubjectClass>();
    }
}
