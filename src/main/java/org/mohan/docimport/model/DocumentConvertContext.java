package org.mohan.docimport.model;

import lombok.Data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 文档转换上下文，封装一次转换过程中需要的基础输入信息。
 */
@Data
public class DocumentConvertContext {

    private String fileName;
    private byte[] content;
    private FileTypeEnum fileType;

    /**
     * 基于当前二进制内容创建新的输入流，供各类解析器使用。
     *
     * @return 新的输入流
     */
    public InputStream newInputStream() {
        return new ByteArrayInputStream(content);
    }
}
