package org.mohan.docimport.converter;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.mohan.docimport.model.DocumentConvertContext;
import org.mohan.docimport.model.FileTypeEnum;
import org.mohan.docimport.model.ImportResult;
import org.mohan.docimport.util.HtmlBuilderUtils;
import org.springframework.stereotype.Component;

/**
 * 老版 Word 文档转换器，主要负责从 .doc 中提取文本内容。
 */
@Component
public class DocFileContentConverter implements FileContentConverter {

    @Override
    public boolean supports(FileTypeEnum fileType) {
        return FileTypeEnum.DOC == fileType;
    }

    @Override
    public ImportResult convert(DocumentConvertContext context) throws Exception {
        try (HWPFDocument document = new HWPFDocument(context.newInputStream());
             WordExtractor extractor = new WordExtractor(document)) {
            StringBuilder html = new StringBuilder();
            for (String paragraph : extractor.getParagraphText()) {
                String text = paragraph == null ? "" : paragraph.trim().replace("\u0007", "");
                if (!text.isBlank()) {
                    html.append(HtmlBuilderUtils.paragraph(text));
                }
            }
            ImportResult result = new ImportResult();
            result.setRichTextContent(HtmlBuilderUtils.wrapDiv(html.toString()));
            return result;
        }
    }
}
