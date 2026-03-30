package org.mohan.docimport.util;

import org.mohan.docimport.model.DownloadedFile;

import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Paths;

/**
 * 远程文件下载工具类，用于根据 URL 拉取文件内容并补全基础元信息。
 */
public final class RemoteFileDownloadUtils {

    private RemoteFileDownloadUtils() {
    }

    /**
     * 下载远程文件并组装为统一的下载结果对象。
     *
     * @param fileUrl 远程文件地址
     * @return 下载结果
     */
    public static DownloadedFile download(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalArgumentException("contentFileUrl 不能为空");
        }
        try {
            URI uri = URI.create(fileUrl);
            URLConnection connection = uri.toURL().openConnection();
            connection.connect();
            try (InputStream inputStream = connection.getInputStream()) {
                DownloadedFile file = new DownloadedFile();
                file.setFileName(resolveFileName(uri));
                file.setContentType(connection.getContentType());
                file.setContent(inputStream.readAllBytes());
                return file;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("下载远程文件失败: " + fileUrl, ex);
        }
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
