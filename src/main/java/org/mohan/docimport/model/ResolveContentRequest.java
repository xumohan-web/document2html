package org.mohan.docimport.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 正文解析请求对象，用于承接已有正文和远程文件地址。
 */
@Data
@Accessors(chain = true)
public class ResolveContentRequest {

    private String content;
    private String contentFileUrl;
}
