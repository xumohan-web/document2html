package org.mohan.docimport.converter;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.mohan.docimport.model.DocumentConvertContext;
import org.mohan.docimport.model.FileTypeEnum;
import org.mohan.docimport.model.ImportResult;
import org.mohan.docimport.util.HtmlBuilderUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * PDF 转换器，侧重文本抽取并按段落转换为 HTML。
 */
@Component
public class PdfFileContentConverter implements FileContentConverter {

    @Override
    public boolean supports(FileTypeEnum fileType) {
        return FileTypeEnum.PDF == fileType;
    }

    @Override
    public ImportResult convert(DocumentConvertContext context) throws Exception {
        try (PDDocument document = Loader.loadPDF(context.getContent())) {
            String text = new PDFTextStripper().getText(document);
            String html = Arrays.stream(text.replace("\r\n", "\n").split("\\n\\s*\\n"))
                    .map(String::trim)
                    .filter(item -> !item.isBlank())
                    .map(HtmlBuilderUtils::paragraph)
                    .collect(Collectors.joining());
            return ImportResult.ofHtml(HtmlBuilderUtils.wrapDiv(html));
        }
    }
}
