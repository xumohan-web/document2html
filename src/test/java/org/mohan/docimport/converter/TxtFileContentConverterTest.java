package org.mohan.docimport.converter;

import org.junit.jupiter.api.Test;
import org.mohan.docimport.model.DocumentConvertContext;
import org.mohan.docimport.model.FileTypeEnum;
import org.mohan.docimport.model.ImportResult;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TxtFileContentConverterTest {

    private final TxtFileContentConverter converter = new TxtFileContentConverter();

    @Test
    void shouldSupportTxtOnly() {
        assertTrue(converter.supports(FileTypeEnum.TXT));
    }

    @Test
    void shouldConvertParagraphsAndEscapeHtml() {
        DocumentConvertContext context = new DocumentConvertContext();
        context.setFileType(FileTypeEnum.TXT);
        context.setContent("first line\nsecond line\n\n  <tag>  ".getBytes(StandardCharsets.UTF_8));

        ImportResult result = converter.convert(context);

        assertEquals("<div><p>first line<br/>second line</p><p>&lt;tag&gt;</p></div>", result.getHtmlContent());
    }
}
