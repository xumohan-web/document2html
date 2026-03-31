package org.mohan.docimport.converter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.mohan.docimport.model.DocumentConvertContext;
import org.mohan.docimport.model.FileTypeEnum;
import org.mohan.docimport.model.ImportResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfFileContentConverterTest {

    private final PdfFileContentConverter converter = new PdfFileContentConverter();

    @Test
    void shouldSupportPdfOnly() {
        assertTrue(converter.supports(FileTypeEnum.PDF));
    }

    @Test
    void shouldConvertPdfParagraphs() throws Exception {
        DocumentConvertContext context = new DocumentConvertContext();
        context.setFileType(FileTypeEnum.PDF);
        context.setContent(createPdfBytes());

        ImportResult result = converter.convert(context);

        assertEquals("<div><p>Alpha<br/>Beta</p></div>", result.getHtmlContent());
    }

    private byte[] createPdfBytes() throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("Alpha");
                contentStream.newLineAtOffset(0, -30);
                contentStream.showText("Beta");
                contentStream.endText();
            }
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }
}
