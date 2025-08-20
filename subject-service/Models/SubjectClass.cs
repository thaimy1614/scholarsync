using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;

namespace subject_service.Models
{
    [Table("subject_classes")]
    public partial class SubjectClass
    {
        [Column("subject-id")]
        public long SubjectId { get; set; }

        [Column("class-id")]
        public long ClassId { get; set; }

        ////so tiet trong tuan
        //[Column("weekly_slots")]
        //public int WeeklySlots { get; set; }

        ////so tiet trong buoi
        //[Column("max_slots_per_session")]
        //public int MaxSlotsPerSession { get; set; }

        ////tiet doi
        //[Column("prefer_consecutive")]
        //public bool PreferConsecutive { get; set; }

        ////mon hoc chinh
        //[Column("is_main_subject")]
        //public bool IsMainSubject { get; set; }

        ////tiet dac biet
        //[Column("special_slot")]
        //public int? SpecialSlot { get; set; }

        ////phong dac biet
        //[Column("special_room")]
        //public long? SpecialRoom { get; set; }

        [Column("is_deleted")]
        public bool IsDeleted { get; set; } = false;

        [JsonIgnore]
        public virtual Subject? Subject { get; set; }
    }
}
