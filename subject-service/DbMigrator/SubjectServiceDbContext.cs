using Microsoft.EntityFrameworkCore;
using subject_service.Models;
using System.Diagnostics;

namespace subject_service.DbMigrator
{
    public class SubjectServiceDbContext : DbContext
    {
        public SubjectServiceDbContext(DbContextOptions<SubjectServiceDbContext> options) : base(options) { }

        public DbSet<Subject> subjects { get; set; }
        public DbSet<Mark> marks { get; set; }
        public DbSet<MarkSubject> markSubjects { get; set; }
        public DbSet<ComponentPoint> componentPoints { get; set; }
        public DbSet<TeacherSubjectClass> teacherSubjectClasses { get; set; }
        public DbSet<MarkType> markTypes { get; set; }
        public DbSet<SubjectClass> subjectClasses { get; set; }
        public DbSet<ViewStudentSubjectScore> viewStudentSubjectScores { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            modelBuilder.Entity<Subject>().HasQueryFilter(p => !p.IsDeleted);
            modelBuilder.Entity<Mark>().HasQueryFilter(p => !p.IsDeleted);
            modelBuilder.Entity<MarkSubject>().HasQueryFilter(p => !p.IsDeleted);
            modelBuilder.Entity<ComponentPoint>().HasQueryFilter(p => !p.IsDeleted);
            modelBuilder.Entity<TeacherSubjectClass>().HasQueryFilter(p => !p.IsDeleted);
            modelBuilder.Entity<MarkType>().HasQueryFilter(p => !p.IsDeleted);
            modelBuilder.Entity<SubjectClass>().HasQueryFilter(p => !p.IsDeleted);

            modelBuilder.Entity<Subject>(entity =>
            {
                entity.HasKey(s => s.Id);
                entity.Property(s => s.Name).IsRequired().HasMaxLength(50);
                entity.Property(s => s.WeeklySlots).IsRequired();
                entity.Property(s => s.MaxSlotsPerSession).IsRequired();
                entity.Property(s => s.PreferConsecutive).IsRequired();
                entity.Property(s => s.IsMainSubject).IsRequired();
                entity.Property(s => s.IsScoreable).IsRequired();
                entity.Property(s => s.IsEvaluateWithScore).IsRequired();
                entity.Property(s => s.SpecialSlot);
                entity.Property(s => s.SpecialRoom);
                entity.Property(s => s.SchoolYearId).IsRequired();
                entity.Property(s => s.IsDeleted).IsRequired();
            });

            modelBuilder.Entity<Mark>(entity =>
            {
                entity.HasKey(s => s.Id);
                //entity.Property(s => s.ScoreAverage);
                entity.Property(s => s.Weight);
                entity.Property(s => s.AcademicPerformance).HasMaxLength(20);
                entity.Property(s => s.Conduct).HasMaxLength(20);
                entity.Property(s => s.AwardTitle).HasMaxLength(20);
                entity.Property(s => s.IsPassed);
                entity.Property(s => s.StudentId).IsRequired().HasMaxLength(255);
                entity.Property(s => s.ClassId).IsRequired();
                entity.Property(s => s.SemesterId);
                entity.Property(s => s.SchoolYearId).IsRequired();
                entity.Property(s => s.IsDeleted).IsRequired();
            });

            modelBuilder.Entity<MarkSubject>(entity =>
            {
                entity.HasKey(s => s.Id);
                entity.Property(s => s.ScoreAverage);
                entity.Property(s => s.QualitativeScoreAverage);
                entity.Property(s => s.MarkId).IsRequired();
                entity.Property(s => s.SubjectId).IsRequired();
                entity.Property(s => s.Note).HasMaxLength(255);
                entity.Property(s => s.IsDeleted).IsRequired();

                entity.HasOne(g => g.Mark)
                      .WithMany(s => s.SubjectMarks)
                      .HasForeignKey(g => g.MarkId)
                      .OnDelete(DeleteBehavior.ClientSetNull);

                entity.HasOne(g => g.Subject)
                      .WithMany(s => s.SubjectMarks)
                      .HasForeignKey(g => g.SubjectId)
                      .OnDelete(DeleteBehavior.ClientSetNull);
            });

            modelBuilder.Entity<ComponentPoint>(entity =>
            {
                entity.HasKey(s => s.Id);
                entity.Property(s => s.Score);
                entity.Property(s => s.QualitativeScore);
                entity.Property(s => s.ColumnOrder).IsRequired();
                entity.Property(s => s.MarkSubjectId).IsRequired();
                entity.Property(s => s.MarkTypeId).IsRequired();
                entity.Property(s => s.IsPassFailType).IsRequired();
                entity.Property(s => s.IsDeleted).IsRequired();

                entity.HasOne(g => g.MarkSubject)
                      .WithMany(s => s.ComponentPoints)
                      .HasForeignKey(g => g.MarkSubjectId)
                      .OnDelete(DeleteBehavior.ClientSetNull);

                entity.HasOne(g => g.MarkType)
                      .WithMany(s => s.ComponentPoints)
                      .HasForeignKey(g => g.MarkTypeId)
                      .OnDelete(DeleteBehavior.ClientSetNull);
            });

            modelBuilder.Entity<MarkType>(entity =>
            {
                entity.HasKey(s => s.Id);
                entity.Property(s => s.Name).IsRequired().HasMaxLength(50);
                entity.Property(s => s.Weight).IsRequired();
                entity.Property(s => s.TotalColumn).IsRequired();
                entity.Property(s => s.ClassId).IsRequired();
                entity.Property(s => s.IsDeleted).IsRequired();

                entity.HasOne(g => g.Subject)
                      .WithMany(s => s.MarkTypes)
                      .HasForeignKey(g => g.SubjectId)
                      .OnDelete(DeleteBehavior.ClientSetNull);
            });

            modelBuilder.Entity<TeacherSubjectClass>(entity =>
            {
                entity.HasKey(s => new { s.TeacherId, s.SubjectId, s.ClassId});
                entity.Property(s => s.TeacherId).IsRequired();
                entity.Property(s => s.SubjectId).IsRequired().HasMaxLength(255);
                entity.Property(s => s.ClassId).IsRequired();
                entity.Property(s => s.IsDeleted).IsRequired();

                entity.HasOne(g => g.Subject)
                      .WithMany(s => s.TeacherSubjectClasses)
                      .HasForeignKey(g => g.SubjectId)
                      .OnDelete(DeleteBehavior.ClientSetNull);
            });
            
            modelBuilder.Entity<SubjectClass>(entity =>
            {
                entity.HasKey(s => new { s.SubjectId, s.ClassId});
                entity.Property(s => s.SubjectId).IsRequired().HasMaxLength(255);
                entity.Property(s => s.ClassId).IsRequired();
                entity.Property(s => s.IsDeleted).IsRequired();

                entity.HasOne(g => g.Subject)
                      .WithMany(s => s.SubjectClasses)
                      .HasForeignKey(g => g.SubjectId)
                      .OnDelete(DeleteBehavior.ClientSetNull);
            });

            modelBuilder.Entity<ViewStudentSubjectScore>()
                .ToView("view_student_subject_scores")
                .HasNoKey();
        }
    }
}
