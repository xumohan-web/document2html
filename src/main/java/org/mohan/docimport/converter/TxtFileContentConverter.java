package org.mohan.docimport.converter;

import org.mohan.docimport.model.DocumentConvertContext;
import org.mohan.docimport.model.FileTypeEnum;
import org.mohan.docimport.model.ImportResult;
import org.mohan.docimport.util.HtmlBuilderUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 纯文本文件转换器，将文本按空行分段后输出为 HTML 段落。
 */
@Component
public class TxtFileContentConverter implements FileContentConverter {

    @Override
    public boolean supports(FileTypeEnum fileType) {
        return FileTypeEnum.TXT == fileType;
    }

    @Override
    public ImportResult convert(DocumentConvertContext context) {
        String text = new String(context.getContent(), StandardCharsets.UTF_8);
        String html = Arrays.stream(text.replace("\r\n", "\n").split("\\n\\s*\\n"))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .map(HtmlBuilderUtils::paragraph)
                .collect(Collectors.joining());
        return ImportResult.ofHtml(HtmlBuilderUtils.wrapDiv(html));
    }
}
