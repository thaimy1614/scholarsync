package com.datn.resource_service.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileUtil {

    public static String readFileContent(File file) throws IOException {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".txt")) {
            return Files.readString(file.toPath());
        } else if (fileName.endsWith(".pdf")) {
            return readPdfContent(file);
        } else if (fileName.endsWith(".docx")) {
            return readDocxContent(file);
        } else if (fileName.endsWith(".pptx")) {
            return readPptxContent(file);
        } else {
            throw new IllegalArgumentException("Unsupported file type. Only TXT, PDF, DOCX, PPTX are supported.");
        }
    }

    private static String readPdfContent(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private static String readDocxContent(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {

            StringBuilder text = new StringBuilder();
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph para : paragraphs) {
                text.append(para.getText()).append("\n");
            }
            return text.toString();
        }
    }

    private static String readPptxContent(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XMLSlideShow ppt = new XMLSlideShow(fis)) {

            StringBuilder text = new StringBuilder();
            ppt.getSlides().forEach(slide -> {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        text.append(textShape.getText()).append("\n");
                    }
                }
            });
            return text.toString();
        }
    }
}
