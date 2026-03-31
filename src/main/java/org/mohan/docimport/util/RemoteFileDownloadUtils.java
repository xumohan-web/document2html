package org.mohan.docimport.util;

import org.mohan.docimport.config.DocumentImportProperties;
import org.mohan.docimport.exception.DocumentImportException;
import org.mohan.docimport.exception.FileDownloadException;
import org.mohan.docimport.exception.InvalidFileException;
import org.mohan.docimport.model.DownloadedFile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

/**
 * 远程文件下载工具类，用于根据 URL 拉取文件内容并补全基础元信息。
 */
@Component
public class RemoteFileDownloadUtils {

    private final DocumentImportProperties properties;

    public RemoteFileDownloadUtils(DocumentImportProperties properties) {
        this.properties = properties;
    }

    /**
     * 下载远程文件并组装为统一的下载结果对象。
     *
     * @param fileUrl 远程文件地址
     * @return 下载结果
     */
    public DownloadedFile download(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new InvalidFileException("contentFileUrl 不能为空");
        }
        try {
            URI uri = URI.create(fileUrl);
            validateUri(uri);
            URLConnection connection = uri.toURL().openConnection();
            connection.setConnectTimeout((int) properties.getDownload().getConnectTimeout().toMillis());
            connection.setReadTimeout((int) properties.getDownload().getReadTimeout().toMillis());
            connection.connect();
            try (InputStream inputStream = connection.getInputStream()) {
                DownloadedFile file = new DownloadedFile();
                file.setFileName(resolveFileName(uri));
                file.setContentType(connection.getContentType());
                file.setContent(readWithLimit(inputStream));
                return file;
            }
        } catch (DocumentImportException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new FileDownloadException("下载远程文件失败: " + fileUrl, ex);
        }
    }

    private void validateUri(URI uri) {
        String scheme = uri.getScheme();
        if (!StringUtils.hasText(scheme)) {
            throw new InvalidFileException("contentFileUrl 缺少协议");
        }
        String normalizedScheme = scheme.toLowerCase();
        if (!"http".equals(normalizedScheme) && !"https".equals(normalizedScheme)) {
            throw new InvalidFileException("仅支持 http/https 协议的远程文件地址");
        }
        String host = uri.getHost();
        if (!StringUtils.hasText(host)) {
            throw new InvalidFileException("contentFileUrl 缺少主机名");
        }
        validateAllowedHost(host);
        validatePublicHost(host);
    }

    private void validateAllowedHost(String host) {
        List<String> allowedHosts = properties.getDownload().getAllowedHosts();
        if (allowedHosts == null || allowedHosts.isEmpty()) {
            return;
        }
        String normalizedHost = normalizeHost(host);
        boolean matched = allowedHosts.stream()
                .filter(StringUtils::hasText)
                .map(this::normalizeHost)
                .anyMatch(normalizedHost::equals);
        if (!matched) {
            throw new InvalidFileException("远程文件主机不在白名单内: " + host);
        }
    }

    private void validatePublicHost(String host) {
        if (properties.getDownload().isAllowPrivateHosts()) {
            return;
        }
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress address : addresses) {
                if (address.isAnyLocalAddress()
                        || address.isLoopbackAddress()
                        || address.isSiteLocalAddress()
                        || address.isLinkLocalAddress()
                        || address.isMulticastAddress()) {
                    throw new InvalidFileException("禁止访问内网或本地主机地址: " + host);
                }
            }
        } catch (DocumentImportException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidFileException("无法解析远程文件地址主机: " + host, ex);
        }
    }

    private String normalizeHost(String host) {
        return host.trim().toLowerCase(Locale.ROOT);
    }

    private byte[] readWithLimit(InputStream inputStream) throws java.io.IOException {
        int maxBytes = (int) properties.getDownload().getMaxSize().toBytes();
        byte[] buffer = new byte[8192];
        int read;
        int total = 0;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        while ((read = inputStream.read(buffer)) != -1) {
            total += read;
            if (total > maxBytes) {
                throw new InvalidFileException("远程文件超过大小限制: " + properties.getDownload().getMaxSize());
            }
            outputStream.write(buffer, 0, read);
        }
        return outputStream.toByteArray();
    }

    /**
     * 根据 URL 路径解析文件名。
     *
     * @param uri 远程文件 URI
     * @return 文件名
     */
    private static String resolveFileName(URI uri) {
        String path = uri.getPath();
        if (path == null || path.isBlank()) {
            return "unknown.bin";
        }
        String fileName = Paths.get(path).getFileName().toString();
        return fileName.isBlank() ? "unknown.bin" : fileName;
    }
}
