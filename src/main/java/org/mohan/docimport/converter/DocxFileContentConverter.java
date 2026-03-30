package org.mohan.docimport.converter;

import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.mohan.docimport.model.DocumentConvertContext;
import org.mohan.docimport.model.FileTypeEnum;
import org.mohan.docimport.model.ImportResult;
import org.mohan.docimport.util.HtmlBuilderUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Word 2007+ 文档转换器，负责解析段落、基础文本样式和表格。
 */
@Component
public class DocxFileContentConverter implements FileContentConverter {

    @Override
    public boolean supports(FileTypeEnum fileType) {
        return FileTypeEnum.DOCX == fileType;
    }

    @Override
    public ImportResult convert(DocumentConvertContext context) throws Exception {
        try (XWPFDocument document = new XWPFDocument(context.newInputStream())) {
            StringBuilder html = new StringBuilder("<div>");
            for (IBodyElement bodyElement : document.getBodyElements()) {
                if (bodyElement.getElementType() == BodyElementType.PARAGRAPH) {
                    html.append(buildParagraph((XWPFParagraph) bodyElement));
                } else if (bodyElement.getElementType() == BodyElementType.TABLE) {
                    html.append(buildTable((XWPFTable) bodyElement));
                }
            }
            html.append("</div>");
            ImportResult result = new ImportResult();
            result.setRichTextContent(html.toString());
            return result;
        }
    }

    /**
     * 将段落节点转换为 HTML 段落，并保留基础文字样式。
     */
    private String buildParagraph(XWPFParagraph paragraph) {
        StringBuilder builder = new StringBuilder();
        for (XWPFRun run : paragraph.getRuns()) {
            String text = run.text();
            if (text == null || text.isBlank()) {
                continue;
            }
            String escaped = HtmlBuilderUtils.escape(text).replace("\n", "<br/>");
            List<String> styles = new ArrayList<>();
            if (run.isBold()) {
                styles.add("font-weight:bold");
            }
            if (run.isItalic()) {
                styles.add("font-style:italic");
            }
            if (run.getUnderline() != UnderlinePatterns.NONE) {
                styles.add("text-decoration:underline");
            }
            if (styles.isEmpty()) {
                builder.append(escaped);
            } else {
                builder.append("<span style=\"")
                        .append(String.join(";", styles))
                        .append("\">")
                        .append(escaped)
                        .append("</span>");
            }
        }
        if (builder.length() == 0) {
            return HtmlBuilderUtils.paragraph("");
        }
        return "<p>" + builder + "</p>";
    }

    /**
     * 将 Word 表格转换为普通 HTML 表格。
     */
    private String buildTable(XWPFTable table) {
        List<List<String>> rows = new ArrayList<>();
        for (XWPFTableRow row : table.getRows()) {
            List<String> cells = new ArrayList<>();
            for (XWPFTableCell cell : row.getTableCells()) {
                cells.add(cell.getText());
            }
            rows.add(cells);
        }
        return HtmlBuilderUtils.table(rows);
    }
}
