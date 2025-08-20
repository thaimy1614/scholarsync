namespace subject_service.Common.Exporting.Interfaces
{
    public interface IMarkExporter
    {
        // --- GVCN ---
        Task<(ExportResult, int)> ExportMarkSummaryBySemesterAsync(long classId, long semesterId, string studentId, int option);

        Task<(ExportResult, int)> ExportMarkSummaryAnnualAsync(long classId, long schoolYearId);

        // --- GVBM ---
        Task<(ExportResult, int)> ExportSubjectScoreBySemesterAsync(long subjectId, long classId, long semesterId, string studentId, int option);

    }
}
