package org.mohan.docimport.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mohan.docimport.converter.FileContentConverter;
import org.mohan.docimport.exception.FileConversionException;
import org.mohan.docimport.model.DownloadedFile;
import org.mohan.docimport.model.FileTypeEnum;
import org.mohan.docimport.model.ImportResult;
import org.mohan.docimport.util.FileTypeDetector;
import org.mohan.docimport.util.RemoteFileDownloadUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentImportServiceImplTest {

    @Mock
    private FileContentConverter converter;

    @Mock
    private FileTypeDetector fileTypeDetector;

    @Mock
    private RemoteFileDownloadUtils remoteFileDownloadUtils;

    private DocumentImportServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DocumentImportServiceImpl(List.of(converter), fileTypeDetector, remoteFileDownloadUtils);
    }

    @Test
    void shouldReturnConverterHtml() throws Exception {
        byte[] content = "hello".getBytes(StandardCharsets.UTF_8);
        when(fileTypeDetector.detect("demo.txt", "text/plain", content)).thenReturn(FileTypeEnum.TXT);
        when(converter.supports(FileTypeEnum.TXT)).thenReturn(true);
        when(converter.convert(any())).thenReturn(ImportResult.ofHtml("<div><p>ok</p></div>"));

        String result = service.importFile("demo.txt", content, "text/plain");

        assertEquals("<div><p>ok</p></div>", result);
    }

    @Test
    void shouldWrapConverterFailure() throws Exception {
        byte[] content = "hello".getBytes(StandardCharsets.UTF_8);
        when(fileTypeDetector.detect("demo.txt", "text/plain", content)).thenReturn(FileTypeEnum.TXT);
        when(converter.supports(FileTypeEnum.TXT)).thenReturn(true);
        when(converter.convert(any())).thenThrow(new RuntimeException("boom"));

        assertThrows(FileConversionException.class,
                () -> service.importFile("demo.txt", content, "text/plain"));
    }

    @Test
    void shouldDownloadRemoteFileWhenResolvingContent() throws Exception {
        DownloadedFile downloadedFile = new DownloadedFile();
        downloadedFile.setFileName("demo.txt");
        downloadedFile.setContentType("text/plain");
        downloadedFile.setContent("hello".getBytes(StandardCharsets.UTF_8));

        when(remoteFileDownloadUtils.download("https://example.com/demo.txt")).thenReturn(downloadedFile);
        when(fileTypeDetector.detect("demo.txt", "text/plain", downloadedFile.getContent())).thenReturn(FileTypeEnum.TXT);
        when(converter.supports(FileTypeEnum.TXT)).thenReturn(true);
        when(converter.convert(any())).thenReturn(ImportResult.ofHtml("<div><p>remote</p></div>"));

        String result = service.resolveContent("", "https://example.com/demo.txt");

        assertEquals("<div><p>remote</p></div>", result);
        verify(remoteFileDownloadUtils).download("https://example.com/demo.txt");
    }

    @Test
    void shouldKeepExistingContentWhenProvided() {
        String result = service.resolveContent("<p>existing</p>", "https://example.com/demo.txt");

        assertEquals("<p>existing</p>", result);
    }
}
