package org.mohan.docimport.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件转换结果，统一收口为单一 HTML 输出。
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ImportResult {

    private final String htmlContent;

    public static ImportResult ofHtml(String htmlContent) {
        return new ImportResult(htmlContent == null ? "" : htmlContent);
    }
}
