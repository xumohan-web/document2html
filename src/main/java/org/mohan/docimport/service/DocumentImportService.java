package org.mohan.docimport.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文档导入服务统一入口，屏蔽不同文件来源和文件格式的差异。
 */
public interface DocumentImportService {

    /**
     * 直接导入上传文件。
     *
     * @param file 上传文件
     * @return 转换后的 HTML 内容
     */
    String importFile(MultipartFile file);

    /**
     * 导入已经在内存中的文件内容。
     *
     * @param fileName 文件名
     * @param content 文件二进制内容
     * @param contentType 文件内容类型
     * @return 转换后的 HTML 内容
     */
    String importFile(String fileName, byte[] content, String contentType);

    /**
     * 当正文为空时，通过远程文件地址解析正文内容。
     *
     * @param content 已有正文
     * @param contentFileUrl 远程文件地址
     * @return 最终正文内容
     */
    String resolveContent(String content, String contentFileUrl);
}
