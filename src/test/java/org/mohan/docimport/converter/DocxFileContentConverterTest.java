package org.mohan.docimport.converter;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.Test;
import org.mohan.docimport.model.DocumentConvertContext;
import org.mohan.docimport.model.FileTypeEnum;
import org.mohan.docimport.model.ImportResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocxFileContentConverterTest {

    private final DocxFileContentConverter converter = new DocxFileContentConverter();

    @Test
    void shouldSupportDocxOnly() {
        assertTrue(converter.supports(FileTypeEnum.DOCX));
    }

    @Test
    void shouldConvertStyledParagraphAndTable() throws Exception {
        DocumentConvertContext context = new DocumentConvertContext();
        context.setFileType(FileTypeEnum.DOCX);
        context.setContent(createDocxBytes());

        ImportResult result = converter.convert(context);

        assertEquals(
                "<div><p><span style=\"font-weight:bold\">Bold</span> normal</p>" +
                        "<table border=\"1\" cellspacing=\"0\" cellpadding=\"6\"><tr><td>A1</td><td>B1</td></tr></table></div>",
                result.getHtmlContent()
        );
    }

    @Test
    void shouldConvertHeadingParagraphAsHeadingTag() throws Exception {
        DocumentConvertContext context = new DocumentConvertContext();
        context.setFileType(FileTypeEnum.DOCX);
        context.setContent(createHeadingDocxBytes());

        ImportResult result = converter.convert(context);

        assertEquals("<div><h1>Overview</h1><p>Body</p></div>", result.getHtmlContent());
    }

    private byte[] createDocxBytes() throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun boldRun = paragraph.createRun();
            boldRun.setBold(true);
            boldRun.setText("Bold");
            XWPFRun normalRun = paragraph.createRun();
            normalRun.setText(" normal");

            XWPFTable table = document.createTable(1, 2);
            table.getRow(0).getCell(0).setText("A1");
            table.getRow(0).getCell(1).setText("B1");

            document.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] createHeadingDocxBytes() throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XWPFParagraph heading = document.createParagraph();
            heading.setStyle("Heading1");
            heading.createRun().setText("Overview");

            XWPFParagraph body = document.createParagraph();
            body.createRun().setText("Body");

            document.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
