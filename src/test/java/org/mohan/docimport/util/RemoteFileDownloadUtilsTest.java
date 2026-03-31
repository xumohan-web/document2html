package org.mohan.docimport.util;

import org.junit.jupiter.api.Test;
import org.mohan.docimport.config.DocumentImportProperties;
import org.mohan.docimport.exception.InvalidFileException;
import org.springframework.util.unit.DataSize;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RemoteFileDownloadUtilsTest {

    @Test
    void shouldRejectUnsupportedProtocol() {
        RemoteFileDownloadUtils utils = new RemoteFileDownloadUtils(defaultProperties());

        assertThrows(InvalidFileException.class,
                () -> utils.download("ftp://example.com/demo.docx"));
    }

    @Test
    void shouldRejectPrivateHostByDefault() {
        RemoteFileDownloadUtils utils = new RemoteFileDownloadUtils(defaultProperties());

        assertThrows(InvalidFileException.class,
                () -> utils.download("http://127.0.0.1/demo.docx"));
    }

    @Test
    void shouldRejectHostOutsideWhitelist() {
        DocumentImportProperties properties = defaultProperties();
        properties.getDownload().setAllowedHosts(List.of("docs.example.com"));
        RemoteFileDownloadUtils utils = new RemoteFileDownloadUtils(properties);

        assertThrows(InvalidFileException.class,
                () -> utils.download("https://example.com/demo.docx"));
    }

    private DocumentImportProperties defaultProperties() {
        DocumentImportProperties properties = new DocumentImportProperties();
        properties.getDownload().setConnectTimeout(Duration.ofSeconds(1));
        properties.getDownload().setReadTimeout(Duration.ofSeconds(1));
        properties.getDownload().setMaxSize(DataSize.ofMegabytes(1));
        return properties;
    }
}
