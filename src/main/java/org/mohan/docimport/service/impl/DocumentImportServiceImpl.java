package org.mohan.docimport.service.impl;

import org.mohan.docimport.converter.FileContentConverter;
import org.mohan.docimport.exception.DocumentImportException;
import org.mohan.docimport.exception.FileConversionException;
import org.mohan.docimport.exception.InvalidFileException;
import org.mohan.docimport.exception.UnsupportedFileException;
import org.mohan.docimport.model.DocumentConvertContext;
import org.mohan.docimport.model.DownloadedFile;
import org.mohan.docimport.model.ImportResult;
import org.mohan.docimport.service.DocumentImportService;
import org.mohan.docimport.util.FileTypeDetector;
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
    private final FileTypeDetector fileTypeDetector;
    private final RemoteFileDownloadUtils remoteFileDownloadUtils;

    public DocumentImportServiceImpl(
            List<FileContentConverter> converters,
            FileTypeDetector fileTypeDetector,
            RemoteFileDownloadUtils remoteFileDownloadUtils
    ) {
        this.converters = converters;
        this.fileTypeDetector = fileTypeDetector;
        this.remoteFileDownloadUtils = remoteFileDownloadUtils;
    }

    @Override
    public String importFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("文件不能为空");
        }
        try {
            return importFile(file.getOriginalFilename(), file.getBytes(), file.getContentType());
        } catch (IOException ex) {
            throw new FileConversionException("读取上传文件失败", ex);
        }
    }

    @Override
    public String importFile(String fileName, byte[] content, String contentType) {
        var fileType = fileTypeDetector.detect(fileName, contentType, content);
        FileContentConverter converter = converters.stream()
                .filter(item -> item.supports(fileType))
                .findFirst()
                .orElseThrow(() -> new UnsupportedFileException("未找到匹配的 converter: " + fileType));
        try {
            DocumentConvertContext context = new DocumentConvertContext();
            context.setFileName(fileName);
            context.setFileType(fileType);
            context.setContent(content);

            ImportResult result = converter.convert(context);
            if (result == null) {
                throw new FileConversionException("解析结果为空: " + fileName, null);
            }
            return result.getHtmlContent();
        } catch (DocumentImportException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new FileConversionException("解析文件失败: " + fileName, ex);
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
        DownloadedFile downloadedFile = remoteFileDownloadUtils.download(contentFileUrl);
        return importFile(downloadedFile.getFileName(), downloadedFile.getContent(), downloadedFile.getContentType());
    }
}
