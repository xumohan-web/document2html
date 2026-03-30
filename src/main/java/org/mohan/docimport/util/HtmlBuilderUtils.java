package org.mohan.docimport.util;

import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * HTML 构造工具类，负责文本转义和常见 HTML 结构拼装。
 */
public final class HtmlBuilderUtils {

    private HtmlBuilderUtils() {
    }

    /**
     * 转义文本中的 HTML 特殊字符。
     *
     * @param text 原始文本
     * @return 转义后的文本
     */
    public static String escape(String text) {
        return HtmlUtils.htmlEscape(text == null ? "" : text);
    }

    /**
     * 将一段纯文本包装成 HTML 段落。
     *
     * @param text 原始文本
     * @return 段落 HTML
     */
    public static String paragraph(String text) {
        if (text == null || text.isBlank()) {
            return "<p><br/></p>";
        }
        return "<p>" + escape(text).replace("\r\n", "\n").replace("\n", "<br/>") + "</p>";
    }

    /**
     * 使用 div 包裹现有 HTML 片段。
     *
     * @param html 原始 HTML
     * @return 包裹后的 HTML
     */
    public static String wrapDiv(String html) {
        return "<div>" + (html == null ? "" : html) + "</div>";
    }

    /**
     * 将二维表格数据转换为 HTML table。
     *
     * @param rows 行列数据
     * @return 表格 HTML
     */
    public static String table(List<List<String>> rows) {
        StringBuilder builder = new StringBuilder("<table border=\"1\" cellspacing=\"0\" cellpadding=\"6\">");
        for (List<String> row : rows) {
            builder.append("<tr>");
            for (String cell : row) {
                builder.append("<td>").append(escape(cell)).append("</td>");
            }
            builder.append("</tr>");
        }
        builder.append("</table>");
        return builder.toString();
    }
}
