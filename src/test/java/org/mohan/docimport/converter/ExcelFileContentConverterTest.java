package org.mohan.docimport.converter;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.mohan.docimport.model.DocumentConvertContext;
import org.mohan.docimport.model.FileTypeEnum;
import org.mohan.docimport.model.ImportResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExcelFileContentConverterTest {

    private final ExcelFileContentConverter converter = new ExcelFileContentConverter();

    @Test
    void shouldSupportXlsAndXlsx() {
        assertTrue(converter.supports(FileTypeEnum.XLS));
        assertTrue(converter.supports(FileTypeEnum.XLSX));
    }

    @Test
    void shouldConvertXlsxSheetToHtmlTable() throws Exception {
        DocumentConvertContext context = new DocumentConvertContext();
        context.setFileType(FileTypeEnum.XLSX);
        context.setContent(createXlsxBytes());

        ImportResult result = converter.convert(context);

        assertEquals(
                "<div><section data-sheet=\"Budget\"><p><strong>Budget</strong></p>" +
                        "<table border=\"1\" cellspacing=\"0\" cellpadding=\"6\"><tr><td>Item</td><td>Total</td></tr>" +
                        "<tr><td>Energy</td><td>3</td></tr></table></section></div>",
                result.getHtmlContent()
        );
    }

    @Test
    void shouldConvertXlsSheetToHtmlTable() throws Exception {
        DocumentConvertContext context = new DocumentConvertContext();
        context.setFileType(FileTypeEnum.XLS);
        context.setContent(createXlsBytes());

        ImportResult result = converter.convert(context);

        assertEquals(
                "<div><section data-sheet=\"Legacy\"><p><strong>Legacy</strong></p>" +
                        "<table border=\"1\" cellspacing=\"0\" cellpadding=\"6\"><tr><td>Old</td></tr></table></section></div>",
                result.getHtmlContent()
        );
    }

    @Test
    void shouldRenderMergedCellsWithColspan() throws Exception {
        DocumentConvertContext context = new DocumentConvertContext();
        context.setFileType(FileTypeEnum.XLSX);
        context.setContent(createMergedXlsxBytes());

        ImportResult result = converter.convert(context);

        assertEquals(
                "<div><section data-sheet=\"Merged\"><p><strong>Merged</strong></p>" +
                        "<table border=\"1\" cellspacing=\"0\" cellpadding=\"6\"><tr><td colspan=\"2\">Header</td></tr>" +
                        "<tr><td>Left</td><td>Right</td></tr></table></section></div>",
                result.getHtmlContent()
        );
    }

    private byte[] createXlsxBytes() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Budget");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Item");
            header.createCell(1).setCellValue("Total");
            Row data = sheet.createRow(1);
            data.createCell(0).setCellValue("Energy");
            data.createCell(1).setCellFormula("1+2");
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] createXlsBytes() throws IOException {
        try (HSSFWorkbook workbook = new HSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Legacy");
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("Old");
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] createMergedXlsxBytes() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Merged");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Header");
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("Left");
            row.createCell(1).setCellValue("Right");

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
