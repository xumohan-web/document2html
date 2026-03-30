package org.mohan.docimport.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件转换结果，既支持富文本 HTML，也支持表格 HTML 列表。
 */
@Data
public class ImportResult {

    private String richTextContent;
    private List<String> tableHtmlList = new ArrayList<>();
}
