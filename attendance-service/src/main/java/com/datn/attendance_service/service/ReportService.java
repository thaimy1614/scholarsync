package com.datn.attendance_service.service;

import com.datn.attendance_service.dto.response.AttendanceSummaryResponse;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.datn.attendance_service.model.AttendanceSummary;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

@Service
public class ReportService {

    public byte[] generateAttendanceReportPdf(AttendanceSummaryResponse summary) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.add(new Paragraph("Attendance Report"));
            document.add(new Paragraph(String.format("Student ID: %s", summary.getStudentId())));
            document.add(new Paragraph(String.format("Period: %s to %s", summary.getPeriodStartDate(), summary.getPeriodEndDate())));
            document.add(new Paragraph(String.format("Total Slots: %d", summary.getTotalSlots())));
            document.add(new Paragraph(String.format("Present: %d", summary.getPresentSlots())));
            document.add(new Paragraph(String.format("Absent: %d", summary.getAbsentSlots())));
            document.add(new Paragraph(String.format("Late: %d", summary.getLateSlots())));
            document.add(new Paragraph(String.format("Early Leave: %d", summary.getEarlyLeaveSlots())));
            document.add(new Paragraph(String.format("Total Score: %.2f", summary.getTotalScore())));
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF report", e);
        }
        return baos.toByteArray();
    }

    public byte[] generateAttendanceReportCsv(AttendanceSummaryResponse summary) {
        StringWriter writer = new StringWriter();
        writer.append("Student ID,Period Start,Period End,Total Slots,Present,Absent,Late,Early Leave,Total Score\n");
        writer.append(String.format("%s,%s,%s,%d,%d,%d,%d,%d,%.2f\n",
                summary.getStudentId(), summary.getPeriodStartDate(), summary.getPeriodEndDate(),
                summary.getTotalSlots(), summary.getPresentSlots(), summary.getAbsentSlots(),
                summary.getLateSlots(), summary.getEarlyLeaveSlots(), summary.getTotalScore()));
        return writer.toString().getBytes();
    }
}