package org.mohan.docimport.util;

import org.junit.jupiter.api.Test;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.mohan.docimport.exception.InvalidFileException;
import org.mohan.docimport.exception.UnsupportedFileException;
import org.mohan.docimport.model.FileTypeEnum;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileTypeDetectorTest {

    private final FileTypeDetector detector = new FileTypeDetector();

    @Test
    void shouldDetectDocxFromSignature() throws IOException {
        byte[] content = zipBytes("word/document.xml", "<w:document/>");

        FileTypeEnum result = detector.detect("demo.docx", "application/octet-stream", content);

        assertEquals(FileTypeEnum.DOCX, result);
    }

    @Test
    void shouldDetectXlsxFromSignature() throws IOException {
        byte[] content = zipBytes("xl/workbook.xml", "<workbook/>");

        FileTypeEnum result = detector.detect("demo.xlsx", null, content);

        assertEquals(FileTypeEnum.XLSX, result);
    }

    @Test
    void shouldDetectDocFromOle2Signature() throws IOException {
        byte[] content = ole2Bytes("WordDocument");

        FileTypeEnum result = detector.detect("demo.doc", "application/msword", content);

        assertEquals(FileTypeEnum.DOC, result);
    }

    @Test
    void shouldUseContentTypeForTxtWhenNoSignatureExists() {
        byte[] content = "hello world".getBytes(StandardCharsets.UTF_8);

        FileTypeEnum result = detector.detect("demo.txt", "text/plain; charset=UTF-8", content);

        assertEquals(FileTypeEnum.TXT, result);
    }

    @Test
    void shouldRejectMismatchBetweenExtensionAndSignature() {
        byte[] content = "%PDF-1.7".getBytes(StandardCharsets.US_ASCII);

        assertThrows(InvalidFileException.class,
                () -> detector.detect("demo.docx", "application/pdf", content));
    }

    @Test
    void shouldRejectMismatchBetweenExtensionAndContentType() {
        byte[] content = "plain text".getBytes(StandardCharsets.UTF_8);

        assertThrows(InvalidFileException.class,
                () -> detector.detect("demo.txt", "application/pdf", content));
    }

    @Test
    void shouldRejectUnknownFileWithoutHints() {
        byte[] content = "plain text".getBytes(StandardCharsets.UTF_8);

        assertThrows(UnsupportedFileException.class,
                () -> detector.detect("demo.unknown", null, content));
    }

    private byte[] zipBytes(String entryName, String content) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            zipOutputStream.putNextEntry(new ZipEntry(entryName));
            zipOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
            zipOutputStream.closeEntry();
        }
        return outputStream.toByteArray();
    }

    private byte[] ole2Bytes(String entryName) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (POIFSFileSystem fileSystem = new POIFSFileSystem()) {
            fileSystem.createDocument(new ByteArrayInputStream(new byte[]{1, 2, 3}), entryName);
            fileSystem.writeFilesystem(outputStream);
        }
        return outputStream.toByteArray();
    }
}
