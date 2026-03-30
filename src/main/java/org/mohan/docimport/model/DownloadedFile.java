package org.mohan.docimport.model;

import lombok.Data;

/**
 * 远程下载后的文件对象，保存文件名、内容类型和二进制内容。
 */
@Data
public class DownloadedFile {

    private String fileName;

    private String contentType;

    private byte[] content;
}
