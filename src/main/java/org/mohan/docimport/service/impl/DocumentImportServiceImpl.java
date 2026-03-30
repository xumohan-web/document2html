package org.mohan.docimport.service.impl;

import org.mohan.docimport.converter.FileContentConverter;
import org.mohan.docimport.model.DocumentConvertContext;
import org.mohan.docimport.model.DownloadedFile;
import org.mohan.docimport.model.FileTypeEnum;
import org.mohan.docimport.model.ImportResult;
import org.mohan.docimport.service.DocumentImportService;
import org.mohan.docimport.util.RemoteFileDownloadUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 文档导入服务实现，负责识别文件类型、分发转换器并收口最终 HTML。
 */
@Service
public class DocumentImportServiceImpl implements DocumentImportService {

    private final List<FileContentConverter> converters;

    public DocumentImportServiceImpl(List<FileContentConverter> converters) {
        this.converters = converters;
    }

    @Override
    public String importFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        try {
            return importFile(file.getOriginalFilename(), file.getBytes(), file.getContentType());
        } catch (IOException ex) {
            throw new IllegalStateException("读取上传文件失败", ex);
        }
    }

    @Override
    public String importFile(String fileName, byte[] content, String contentType) {
        FileTypeEnum fileType = FileTypeEnum.of(fileName);
        if (fileType == null) {
            throw new IllegalArgumentException("不支持的文件类型: " + fileName);
        }
        FileContentConverter converter = converters.stream()
                .filter(item -> item.supports(fileType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未找到匹配的 converter: " + fileType));
        try {
            DocumentConvertContext context = new DocumentConvertContext();
            context.setFileName(fileName);
            context.setFileType(fileType);
            context.setContent(content);

            ImportResult result = converter.convert(context);
            if (result.getRichTextContent() != null && !result.getRichTextContent().isBlank()) {
                return result.getRichTextContent();
            }
            return "<div>" + String.join("", result.getTableHtmlList()) + "</div>";
        } catch (Exception ex) {
            throw new IllegalStateException("解析文件失败", ex);
        }
    }

    @Override
    public String resolveContent(String content, String contentFileUrl) {
        if (content != null && !content.isBlank()) {
            return content;
        }
        if (contentFileUrl == null || contentFileUrl.isBlank()) {
            return content;
        }
        DownloadedFile downloadedFile = RemoteFileDownloadUtils.download(contentFileUrl);
        return importFile(downloadedFile.getFileName(), downloadedFile.getContent(), downloadedFile.getContentType());
    }
}
