using logbook_service.Models;
using Microsoft.EntityFrameworkCore;

namespace logbook_service.DbMigrator
{
    public class LogbookServiceDbContext : DbContext
    {
        public LogbookServiceDbContext(DbContextOptions<LogbookServiceDbContext> options) : base(options) 
        {}

        public DbSet<CompetitionRule> competitionRules { get; set; }
        public DbSet<CompetitionPeriodScore> competitionPeriodScores { get; set; }
        public DbSet<CompetitionDailyRecord>  competitionDailyRecords { get; set; }
        public DbSet<CompetitionDailyDetail> competitionDailyDetails { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            modelBuilder.Entity<CompetitionRule>().HasQueryFilter(p => !p.isDeleted);
            modelBuilder.Entity<CompetitionPeriodScore>().HasQueryFilter(p => !p.isDeleted);
            modelBuilder.Entity<CompetitionDailyRecord>().HasQueryFilter(p => !p.isDeleted);
            modelBuilder.Entity<CompetitionDailyDetail>().HasQueryFilter(p => !p.isDeleted);

            modelBuilder.Entity<CompetitionRule>(entity =>
            {
                entity.HasKey(e => e.id);
                entity.Property(e => e.name).IsRequired().HasMaxLength(50);
                entity.Property(e => e.type).IsRequired().HasMaxLength(20);
                entity.Property(e => e.score).IsRequired();
                entity.Property(e => e.description).HasMaxLength(500);
                entity.Property(e => e.isDeleted).IsRequired();
            });

            modelBuilder.Entity<CompetitionPeriodScore>(entity =>
            {
                entity.HasKey(e => e.id);
                entity.Property(e => e.classId).IsRequired();
                entity.Property(e => e.teacherId).IsRequired();
                entity.Property(e => e.recordDate).IsRequired();
                entity.Property(e => e.periodNumber).IsRequired();
                entity.Property(e => e.scoreGrade).IsRequired().HasMaxLength(10); 
                entity.Property(e => e.scoreValue).IsRequired();
                entity.Property(e => e.note).HasMaxLength(200);
                entity.Property(e => e.isDeleted).IsRequired();
            });

            modelBuilder.Entity<CompetitionDailyRecord>(entity =>
            {
                entity.HasKey(e => e.id);
                entity.Property(e => e.classId).IsRequired();
                entity.Property(e => e.teacherId).IsRequired();
                entity.Property(e => e.recordDate).IsRequired();
                entity.Property(e => e.totalScore).IsRequired();
                entity.Property(e => e.note).HasMaxLength(200);
                entity.Property(e => e.isDeleted).IsRequired();
            });

            modelBuilder.Entity<CompetitionDailyDetail>(entity =>
            {
                entity.HasKey(e => e.id);
                entity.Property(e => e.competitionDailyRecordId).IsRequired();
                entity.Property(e => e.competitionRuleId).IsRequired();
                entity.Property(e => e.studentId); 
                entity.Property(e => e.score).IsRequired();
                entity.Property(e => e.note).HasMaxLength(500);
                entity.Property(e => e.isDeleted).IsRequired();

                entity.HasOne(g => g.CompetitionDailyRecord)
                      .WithMany(s => s.CompetitionDailyDetails)
                      .HasForeignKey(g => g.competitionDailyRecordId)
                      .OnDelete(DeleteBehavior.ClientSetNull);

                entity.HasOne(g => g.CompetitionRule)
                      .WithMany(s => s.CompetitionDailyDetails)
                      .HasForeignKey(e => e.competitionRuleId)
                      .OnDelete(DeleteBehavior.ClientSetNull);
            });
        }
    }
}
