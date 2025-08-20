package com.datn.school_service.Utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
public class ExcelTemplateUtils {

    public static ByteArrayOutputStream generateStudentEmailTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("StudentEmails");

        // ===== FONT =====
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);

        // ===== TITLE =====
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setFont(boldFont);

        Row titleRow = sheet.createRow(1); // Row 2
        Cell titleCell = titleRow.createCell(1); // Column B
        titleCell.setCellValue("STUDENT EMAIL LIST FOR ADD STUDENT TO CLASS");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 4)); // Merge B2:E2

        // ===== LABEL: "List student email" =====
        Row labelRow = sheet.createRow(4); // Row 5
        Cell labelCell = labelRow.createCell(1); // B5
        labelCell.setCellValue("List student email");

        // Style viền cho label
        CellStyle labelBorder = workbook.createCellStyle();
        labelBorder.setBorderBottom(BorderStyle.THIN);
        labelBorder.setBorderTop(BorderStyle.THIN);
        labelBorder.setBorderLeft(BorderStyle.THIN);
        labelBorder.setBorderRight(BorderStyle.THIN);
        labelCell.setCellStyle(labelBorder);

        // ===== CONDITIONAL FORMATTING: viền tự động khi nhập =====
        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();
        ConditionalFormattingRule rule = sheetCF.createConditionalFormattingRule("NOT(ISBLANK(B6))");

        BorderFormatting borderFmt = rule.createBorderFormatting();
        borderFmt.setBorderBottom(BorderStyle.THIN);
        borderFmt.setBorderTop(BorderStyle.THIN);
        borderFmt.setBorderLeft(BorderStyle.THIN);
        borderFmt.setBorderRight(BorderStyle.THIN);

        CellRangeAddress[] regions = { CellRangeAddress.valueOf("B6:B100") };
        sheetCF.addConditionalFormatting(regions, rule);

        // ===== DATA VALIDATION: chỉ cho nhập email =====
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();

        String formula = "AND(ISNUMBER(SEARCH(\"@\",B6)),ISNUMBER(SEARCH(\".\",B6)))";

        DataValidationConstraint constraint = validationHelper.createCustomConstraint(formula);
        CellRangeAddressList validationRange = new CellRangeAddressList(5, 99, 1, 1); // B6:B100

        DataValidation validation = validationHelper.createValidation(constraint, validationRange);
        validation.setShowErrorBox(true);
        validation.createErrorBox("Invalid Email", "Vui lòng nhập đúng định dạng email (phải có @ và .)");

        sheet.addValidationData(validation);

        // ===== AUTO SIZE COLUMN B =====
        sheet.autoSizeColumn(1);

        // ===== TRẢ VỀ FILE =====
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out;
    }
}