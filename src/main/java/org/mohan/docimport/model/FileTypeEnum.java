package org.mohan.docimport.model;

import java.util.Arrays;

/**
 * 系统支持的文件类型枚举，并提供按文件名识别类型的能力。
 */
public enum FileTypeEnum {

    TXT("txt"),
    DOC("doc"),
    DOCX("docx"),
    PDF("pdf"),
    XLS("xls"),
    XLSX("xlsx");

    private final String extension;

    FileTypeEnum(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    /**
     * 根据文件名后缀识别文件类型。
     *
     * @param fileName 文件名
     * @return 匹配到的文件类型，无法识别时返回 null
     */
    public static FileTypeEnum of(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return null;
        }
        String suffix = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return Arrays.stream(values())
                .filter(item -> item.extension.equals(suffix))
                .findFirst()
                .orElse(null);
    }
}
