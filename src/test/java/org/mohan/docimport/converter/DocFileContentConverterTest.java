package org.mohan.docimport.converter;

import org.junit.jupiter.api.Test;
import org.mohan.docimport.model.DocumentConvertContext;
import org.mohan.docimport.model.FileTypeEnum;
import org.mohan.docimport.model.ImportResult;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocFileContentConverterTest {

    private final DocFileContentConverter converter = new DocFileContentConverter();

    @Test
    void shouldSupportDocOnly() {
        assertTrue(converter.supports(FileTypeEnum.DOC));
    }

    @Test
    void shouldConvertDocParagraphs() throws Exception {
        DocumentConvertContext context = new DocumentConvertContext();
        context.setFileType(FileTypeEnum.DOC);
        context.setContent(loadSampleDoc());

        ImportResult result = converter.convert(context);

        assertEquals("<div><p>First paragraph</p><p>Second paragraph</p></div>", result.getHtmlContent());
    }

    private byte[] loadSampleDoc() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/samples/simple.doc")) {
            assertNotNull(inputStream, "sample .doc resource should exist");
            return inputStream.readAllBytes();
        }
    }
}
