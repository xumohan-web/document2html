package org.mohan.docimport.util;

import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.mohan.docimport.exception.InvalidFileException;
import org.mohan.docimport.exception.UnsupportedFileException;
import org.mohan.docimport.model.FileTypeEnum;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 基于扩展名、内容类型和文件头综合识别文件类型。
 */
@Component
public class FileTypeDetector {

    private static final Set<String> GENERIC_CONTENT_TYPES = Set.of(
            "application/octet-stream",
            "application/zip",
            "application/x-zip-compressed"
    );

    private static final Map<String, FileTypeEnum> CONTENT_TYPE_MAPPING = Map.ofEntries(
            Map.entry("text/plain", FileTypeEnum.TXT),
            Map.entry("application/pdf", FileTypeEnum.PDF),
            Map.entry("application/msword", FileTypeEnum.DOC),
            Map.entry("application/vnd.openxmlformats-officedocument.wordprocessingml.document", FileTypeEnum.DOCX),
            Map.entry("application/vnd.ms-excel", FileTypeEnum.XLS),
            Map.entry("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", FileTypeEnum.XLSX)
    );

    public FileTypeEnum detect(String fileName, String contentType, byte[] content) {
        if (content == null || content.length == 0) {
            throw new InvalidFileException("文件内容不能为空");
        }

        FileTypeEnum extensionType = FileTypeEnum.of(fileName);
        FileTypeEnum signatureType = detectBySignature(content);
        FileTypeEnum declaredType = detectByContentType(contentType);

        validateConsistency(extensionType, declaredType, "文件扩展名");
        validateConsistency(extensionType, signatureType, "文件扩展名");
        validateConsistency(declaredType, signatureType, "Content-Type");

        FileTypeEnum detectedType = signatureType != null ? signatureType : declaredType;
        if (detectedType == null) {
            detectedType = extensionType;
        }
        if (detectedType == null) {
            throw new UnsupportedFileException("无法识别文件类型: " + fileName);
        }
        return detectedType;
    }

    private void validateConsistency(FileTypeEnum declaredType, FileTypeEnum actualType, String label) {
        if (declaredType != null && actualType != null && declaredType != actualType) {
            throw new InvalidFileException(label + "与文件内容不匹配");
        }
    }

    private FileTypeEnum detectByContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return null;
        }
        String normalized = contentType.toLowerCase(Locale.ROOT).split(";")[0].trim();
        if (GENERIC_CONTENT_TYPES.contains(normalized)) {
            return null;
        }
        return CONTENT_TYPE_MAPPING.get(normalized);
    }

    private FileTypeEnum detectBySignature(byte[] content) {
        try (InputStream inputStream = FileMagic.prepareToCheckMagic(new ByteArrayInputStream(content))) {
            FileMagic fileMagic = FileMagic.valueOf(inputStream);
            if (fileMagic == FileMagic.PDF) {
                return FileTypeEnum.PDF;
            }
            if (fileMagic == FileMagic.OLE2) {
                return detectOle2Type(content);
            }
            if (fileMagic == FileMagic.OOXML) {
                return detectOoxmlType(content);
            }
        } catch (IOException ex) {
            throw new InvalidFileException("读取文件头失败", ex);
        }
        return null;
    }

    private FileTypeEnum detectOle2Type(byte[] content) throws IOException {
        try (POIFSFileSystem fileSystem = new POIFSFileSystem(new ByteArrayInputStream(content))) {
            Iterator<org.apache.poi.poifs.filesystem.Entry> entries = fileSystem.getRoot().getEntries();
            while (entries.hasNext()) {
                String entryName = entries.next().getName();
                if ("WordDocument".equals(entryName)) {
                    return FileTypeEnum.DOC;
                }
                if ("Workbook".equals(entryName) || "Book".equals(entryName)) {
                    return FileTypeEnum.XLS;
                }
            }
        }
        return null;
    }

    private FileTypeEnum detectOoxmlType(byte[] content) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(content))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.startsWith("word/")) {
                    return FileTypeEnum.DOCX;
                }
                if (name.startsWith("xl/")) {
                    return FileTypeEnum.XLSX;
                }
            }
        }
        return null;
    }
}
